#!/usr/bin/env python3
"""
Create three users, log them in, send messages between them, and fetch the
conversation list and message history for each user.

Usage:
  python3 scripts/bootstrap_conversations.py [base_url]

Default base URL:
  http://localhost:8080
"""

from __future__ import annotations

import json
import sys
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass
from typing import Any


BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"


@dataclass
class User:
    username: str
    password: str
    id: str | None = None
    token: str | None = None


def http(method: str, path: str, body: dict[str, Any] | None = None, token: str | None = None) -> Any:
    url = urllib.parse.urljoin(BASE_URL.rstrip("/") + "/", path.lstrip("/"))
    headers = {"Accept": "application/json"}
    data = None
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = f"Bearer {token}"

    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            raw = resp.read().decode("utf-8")
            return json.loads(raw) if raw else None
    except urllib.error.HTTPError as exc:
        raw = exc.read().decode("utf-8")
        raise RuntimeError(f"{method} {path} failed with {exc.code}: {raw}") from exc


def register_and_login(user: User) -> None:
    print(f"[login] registering {user.username}")
    http("POST", "/auth/register", {"username": user.username, "password": user.password})
    print(f"[login] logging in {user.username}")
    login = http("POST", "/auth/login", {"username": user.username, "password": user.password})
    user.token = login["accessToken"]
    profile = http("GET", f"/users/{urllib.parse.quote(user.username)}", token=user.token)
    user.id = profile["id"]
    print(f"[login] {user.username} logged in as {user.id}")


def send(sender: User, recipient: User, body: str) -> dict[str, Any]:
    print(f"[send] {sender.username} -> {recipient.username}: {body}")
    return http(
        "POST",
        "/messages",
        {"recipientId": recipient.id, "body": body},
        token=sender.token,
    )


def list_conversations(user: User) -> list[dict[str, Any]]:
    print(f"[fetch] conversations for {user.username}")
    data = http("GET", "/conversations", token=user.token)
    return data if isinstance(data, list) else []


def fetch_history(user: User, conversation_id: str) -> list[dict[str, Any]]:
    print(f"[fetch] history for {user.username} conversation {conversation_id}")
    messages: list[dict[str, Any]] = []
    cursor: str | None = None

    while True:
        query = {"limit": 100}
        if cursor is not None:
            query["cursor"] = cursor

        path = f"/conversations/{conversation_id}/messages?{urllib.parse.urlencode(query)}"
        page = http("GET", path, token=user.token)
        items = page.get("items", []) if isinstance(page, dict) else []
        messages.extend(items)
        print(f"[fetch] received {len(items)} messages")

        cursor = page.get("nextCursor") if isinstance(page, dict) else None
        if not cursor:
            break

    return messages


def main() -> None:
    alice = User("alice_boot", "password123")
    bob = User("bob_boot", "password123")
    charles = User("charles_boot", "password123")
    users = [alice, bob, charles]

    for user in users:
        register_and_login(user)

    send(alice, bob, "alice -> bob")
    send(bob, alice, "bob -> alice")
    send(charles, alice, "charles -> alice")
    send(alice, charles, "alice -> charles")

    print("\nConversation lists:")
    for user in users:
        conversations = list_conversations(user)
        print(f"\n{user.username} ({user.id})")
        print(json.dumps(conversations, indent=2, default=str))

        print("Message history:")
        for conversation in conversations:
            conversation_id = conversation["conversationId"]
            history = fetch_history(user, conversation_id)
            print(f"  conversation {conversation_id}")
            print(json.dumps(history, indent=2, default=str))


if __name__ == "__main__":
    main()
