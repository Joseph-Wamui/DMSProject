import base64
import sys
import requests
import json
import os

def encode_image(image_path):
    with open(image_path, "rb") as image_file:
        return base64.b64encode(image_file.read()).decode('utf-8')

def main():
    # Get the image path from the command-line arguments
    image_path = sys.argv[1]
    keys = sys.argv[2:]

    # Encode the image
    base64_image = encode_image(image_path)

    # Retrieve OpenAI API Key from environment variables
    api_key = os.getenv('OPENAI_API_KEY')

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {api_key}"
    }

    payload = {
        "model": "gpt-4o-mini",
        "messages": [
            {
                "role": "user",
                "content": [
                    {
                        "type": "text",
                        "text": f"""Extract the following key-value pairs from the document: {', '.join(keys)}.
                        The response should only include key-value pairs and no additional text, categories, or explanations.
                        Return full names of keys in their long form as they appear in the document if abbreviated in keys.
                        All dates should be formatted to standard format dd/MM/yyyy even when separated.
                        Ignore any signatures present.
                        Provide the output as a JSON object."""
                    },
                    {
                        "type": "image_url",
                        "image_url": {
                            "url": f"data:image/jpeg;base64,{base64_image}"
                        }
                    }
                ]
            }
        ],
        "max_tokens": 500
    }

    response = requests.post("https://api.openai.com/v1/chat/completions", headers=headers, json=payload)

    # Print the response to standard output
    print(json.dumps(response.json()))

if __name__ == "__main__":
    main()