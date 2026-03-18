#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
阿里云视频点播播放授权工具包

提供播放授权 Token 的生成、验证和解析功能
"""

from .vod_jwt_constants import (
    DEFAULT_APP_ID,
    DEFAULT_PLAY_KEY,
    DEFAULT_REGION_ID,
    EXPIRED_TIME_MILLS
)
from .vod_play_auth_token import (
    generate_token,
    generate_token_with_config,
    verify_token,
    verify_token_with_key,
    parse_payload,
    parse_header
)

__all__ = [
    # 常量
    'DEFAULT_APP_ID',
    'DEFAULT_PLAY_KEY',
    'DEFAULT_REGION_ID',
    'EXPIRED_TIME_MILLS',
    # Token 方法
    'generate_token',
    'generate_token_with_config',
    'verify_token',
    'verify_token_with_key',
    'parse_payload',
    'parse_header'
]
