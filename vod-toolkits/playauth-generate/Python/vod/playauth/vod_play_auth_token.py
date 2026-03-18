#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
阿里云视频点播播放授权 Token 工具类

提供 JWT 格式的播放授权 Token 生成与验证功能:
    - 生成播放授权 Token
    - 验证 Token 有效性与签名
    - 解析 Token 的 Header 和 Payload

重要提示: 使用 vid + JWTPlayAuth（字段名: playAuth）进行播放时，
    客户端播放器 SDK 版本需要满足 >= 7.10.0，否则无法正常完成播放鉴权。

@author: wyq
@date: 2025/9/12 14:28
"""

import time
import jwt
import base64
import json
from typing import Optional

from .vod_jwt_constants import (
    DEFAULT_APP_ID,
    DEFAULT_PLAY_KEY,
    DEFAULT_REGION_ID,
    EXPIRED_TIME_MILLS
)


def generate_token(video_id: str) -> Optional[str]:
    """
    生成播放授权 Token（使用默认配置）

    使用 vod_jwt_constants 模块中定义的默认参数生成 Token

    Args:
        video_id: 视频 ID，不能为空

    Returns:
        JWT Token 字符串，生成失败返回 None
    """
    return generate_token_with_config(
        video_id,
        DEFAULT_APP_ID,
        DEFAULT_PLAY_KEY,
        DEFAULT_REGION_ID,
        EXPIRED_TIME_MILLS
    )


def generate_token_with_config(
        video_id: str,
        app_id: str,
        play_key: str,
        region_id: str,
        expired_time_mills: int
) -> Optional[str]:
    """
    生成播放授权 Token（自定义配置）

    Token Payload 包含以下字段:
        - appId: 应用标识
        - videoId: 视频 ID
        - currentTimeStamp: 签发时间戳（毫秒）
        - expireTimeStamp: 过期时间戳（毫秒）
        - regionId: 地域标识
        - playContentInfo: 播放配置信息

    Args:
        video_id: 视频 ID，不能为空
        app_id: 应用 ID，不能为空
        play_key: 播放密钥，不能为空
        region_id: 地域标识，不能为空
        expired_time_mills: 过期时间（毫秒），必须大于 0

    Returns:
        JWT Token 字符串，生成失败返回 None
    """
    current_time = int(time.time() * 1000)
    expire_time = current_time + expired_time_mills

    # 播放内容配置信息
    # 注意: PlayContentInfo 接口入参与原接口 GetPlayInfo 一致
    play_content_info = {
        "formats": "mp4",
        "authTimeout": 1800,
        "streamType": "video"
    }

    payload = {
        "appId": app_id,
        "videoId": video_id,
        "currentTimeStamp": current_time,
        "expireTimeStamp": expire_time,
        "regionId": region_id,
        "playContentInfo": play_content_info
    }

    try:
        token = jwt.encode(payload, play_key, algorithm="HS256")
        return token
    except Exception as e:
        print(f"JWT Token 创建失败: {e}")
        return None


def verify_token(token: str) -> bool:
    """
    验证播放授权 Token（使用默认密钥）

    验证流程:
        1. Token 格式校验（三段式结构）
        2. 签名验证
        3. 时间戳校验（防重放攻击、过期检查）

    Args:
        token: 待验证的 JWT Token

    Returns:
        True 验证通过，False 验证失败
    """
    return verify_token_with_key(token, DEFAULT_PLAY_KEY)


def verify_token_with_key(token: str, play_key: str) -> bool:
    """
    验证播放授权 Token（自定义密钥）

    Args:
        token: 待验证的 JWT Token
        play_key: 播放密钥

    Returns:
        True 验证通过，False 验证失败
    """
    # 1. Token 格式校验
    parts = token.split(".")
    if len(parts) != 3:
        print("Token 格式无效：应为 Header.Payload.Signature 格式")
        return False

    # 2. 解析 Payload
    try:
        payload_b64 = parts[1]
        missing_padding = len(payload_b64) % 4
        if missing_padding:
            payload_b64 += '=' * (4 - missing_padding)
        payload_json = base64.b64decode(payload_b64).decode('utf-8')
        payload_dict = json.loads(payload_json)
    except Exception as e:
        print(f"Payload 解析失败: {e}")
        return False

    # 3. 签名验证
    try:
        jwt.decode(
            token,
            play_key,
            algorithms=["HS256"],
            options={"require": ["appId", "videoId", "currentTimeStamp", "expireTimeStamp"]}
        )
    except jwt.InvalidTokenError as e:
        print(f"JWT 验证失败: {e}")
        return False

    # 4. 时间戳校验
    now = int(time.time() * 1000)

    current_time_stamp = payload_dict.get("currentTimeStamp")
    if current_time_stamp is None or current_time_stamp > now:
        print("签发时间无效：Token 签发时间不能晚于当前时间")
        return False

    expire_time_stamp = payload_dict.get("expireTimeStamp")
    if expire_time_stamp is not None and expire_time_stamp < now:
        print("Token 已过期：当前时间超过了 Token 的有效期")
        return False

    return True


def parse_payload(token: str) -> Optional[str]:
    """
    解析 Token 的 Payload 部分

    Args:
        token: JWT Token

    Returns:
        Payload JSON 字符串，解析失败返回 None
    """
    try:
        parts = token.split(".")
        if len(parts) != 3:
            return None
        payload_b64 = parts[1]
        missing_padding = len(payload_b64) % 4
        if missing_padding:
            payload_b64 += '=' * (4 - missing_padding)
        return base64.b64decode(payload_b64).decode('utf-8')
    except Exception:
        return None


def parse_header(token: str) -> Optional[str]:
    """
    解析 Token 的 Header 部分

    Args:
        token: JWT Token

    Returns:
        Header JSON 字符串，解析失败返回 None
    """
    try:
        parts = token.split(".")
        if len(parts) != 3:
            return None
        header_b64 = parts[0]
        missing_padding = len(header_b64) % 4
        if missing_padding:
            header_b64 += '=' * (4 - missing_padding)
        return base64.b64decode(header_b64).decode('utf-8')
    except Exception:
        return None
