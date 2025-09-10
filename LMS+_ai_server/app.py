import os
import torch
import re
import requests
from flask import Flask, request, jsonify
from moviepy.video.io.VideoFileClip import VideoFileClip
import whisper
from collections import Counter
from konlpy.tag import Komoran

# ─── Ollama API 설정 ────────────────────────────────────────────────
OLLAMA_API_URL = os.getenv("OLLAMA_API_URL", "http://localhost:11435/api/generate")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "llama3")
OLLAMA_OPTIONS = {"temperature": 0.2, "top_p": 0.95, "repeat_penalty": 1.1}

# 불용어 로딩
STOPWORDS_PATH = os.path.join(os.path.dirname(__file__), 'stop_dict.txt')
def load_stopwords(path=STOPWORDS_PATH):
    stopwords = set()
    try:
        with open(path, encoding='utf-8') as f:
            for line in f:
                w = line.strip().lstrip('\ufeff')
                if w:
                    stopwords.add(w)
    except FileNotFoundError:
        print(f"[ERROR] Stopwords file not found at: {path}")
    return stopwords

STOPWORDS = load_stopwords()
komoran = Komoran()
device = "cuda" if torch.cuda.is_available() else "cpu"
whisper_model = whisper.load_model("small", device=device)

# Flask 앱
app = Flask(__name__)

# 텍스트 추출 & 키워드 전처리
def collect_transcript_and_segments(video_path):
    audio_path = os.path.splitext(video_path)[0] + '.wav'
    clip = VideoFileClip(video_path)
    clip.audio.write_audiofile(audio_path)
    clip.close()
    result = whisper_model.transcribe(audio_path, language="ko")
    return result.get("text", ""), result.get("segments", [])

def preprocess_keep_nouns(text):
    raw_nouns = komoran.nouns(text)
    clean = []
    for n in raw_nouns:
        n_low = n.lower()
        if re.fullmatch(r"[가-힣a-z]+", n_low) and n_low not in STOPWORDS and len(n_low) > 1:
            clean.append(n_low)
    return clean

# Ollama 요약 함수
def summarize_with_ollama(text: str) -> str:
    """
    Ollama 서버에 전사문을 보내 요약을 받고, 반환된 JSON 구조에 따라
    적절한 필드를 꺼내 리턴합니다. 실패 시 빈 문자열을 반환합니다.
    """
    payload = {
        "model": OLLAMA_MODEL,
        "prompt": f"""
    당신은 한국어 전문 영상 요약 엔진입니다. 영상의 핵심 내용을 간결하고 명확하게 요약하되, 불필요한 서론이나 부연 설명 없이 바로 본론부터 시작하세요. 영어 문장은 절대 사용하지 말고, 오직 한국어로만 응답해야 합니다.
    아래 강의 전사문을 영상 요약본처럼 핵심 내용을 중심으로 요약해 주세요.
    {text}
    """,
        "stream": False,
        **OLLAMA_OPTIONS
    }


    response = requests.post(OLLAMA_API_URL, json=payload)
    response.raise_for_status()

    # 응답이 JSON이 아닐 수도 있으니 예외 처리
    try:
        data = response.json()
    except ValueError:
        print("Ollama non-JSON response:", response.text)
        return ""

    # 디버그: 실제 반환된 구조 확인
    print("Ollama response data:", data)

    # 다양한 응답 키 처리
    if isinstance(data, dict):
        # Ollama 기본 응답 키
        if "response" in data:
            return data.get("response", "")
        if "results" in data and isinstance(data["results"], list):
            return data["results"][0].get("generated_text", "")
        if "choices" in data and isinstance(data["choices"], list):
            return data["choices"][0].get("text", "")
        if "generated_text" in data:
            return data.get("generated_text", "")
        if "text" in data:
            return data.get("text", "")
    # 그 외: 전체 문자열 반환
    return str(data)

# Flask API: Spring Boot가 경로 전달 → 분석 결과 반환
@app.route('/api/process', methods=['POST'])
def api_process_video():
    data = request.get_json()
    video_path = data.get("video_path")
    if not video_path or not os.path.exists(video_path):
        return jsonify({"error": "Invalid video path"}), 400

    raw_text, segments = collect_transcript_and_segments(video_path)

    raw_text = raw_text.replace("채찌피티", "ChatGPT")

    tokens = preprocess_keep_nouns(raw_text)
    freq = Counter(tokens)

    # Ollama로 요약
    summary = summarize_with_ollama(raw_text)

    # !!!!! 이 라인을 추가합니다 !!!!!
    # Flask가 Spring Boot에 보내기 직전의 summary 값을 확인합니다.
    print(f"DEBUG (Flask): Final summary value before jsonify: '{summary[:200]}...' (length: {len(summary)})") 

    return jsonify({
        "filename": os.path.basename(video_path),
        "tokens": tokens,
        "top_keywords": freq.most_common(10),
        "segments": segments,
        "raw_text": raw_text,
        "summary": summary
    })

if __name__ == '__main__':
    app.run(debug=True, use_reloader=False)
