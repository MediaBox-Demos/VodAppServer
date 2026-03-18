#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
阿里云视频点播播放授权 Token 示例程序

功能演示:
    - 生成播放授权 Token
    - 解析 Token 的 Header 和 Payload
    - 验证 Token 有效性

@author: wyq
@date: 2025/9/12 14:28
"""

from vod.playauth import (
    generate_token,
    verify_token,
    parse_header,
    parse_payload
)

# 示例视频 ID
# 来源：阿里云视频点播控制台 - 媒资管理 - 视频列表
SAMPLE_VIDEO_ID = "10b71107a99871f097b96732b78e0102"


def main():
    print("========== 阿里云视频点播播放授权 Token 示例 ==========\n")

    # 1. 生成 Token
    print("【步骤1】生成 Token")
    token = generate_token(SAMPLE_VIDEO_ID)
    if token is None:
        print("Token 生成失败！")
        return
    print(f"Token: {token}")
    print()

    # 2. 解析 Token
    print("【步骤2】解析 Token")
    header = parse_header(token)
    payload = parse_payload(token)
    print(f"Header: {header}")
    print(f"Payload: {payload}")
    print()

    # 3. 验证 Token
    print("【步骤3】验证 Token")
    is_valid = verify_token(token)
    print(f"验证结果: {'通过' if is_valid else '失败'}")


if __name__ == "__main__":
    main()
