package main

import (
	"fmt"

	"vod/playauth"
)

// 阿里云视频点播播放授权 Token 示例程序
// 功能演示:
//   - 生成播放授权 Token
//   - 解析 Token 的 Header 和 Payload
//   - 验证 Token 有效性
//
// @author wyq
// @date 2025/9/12 14:28

// 示例视频 ID
// 来源：阿里云视频点播控制台 - 媒资管理 - 视频列表
const sampleVideoID = "10b71107a99871f097b96732b78e0102"

func main() {
	fmt.Println("========== 阿里云视频点播播放授权 Token 示例 ==========\n")

	// 1. 生成 Token
	fmt.Println("【步骤1】生成 Token")
	token, err := playauth.GenerateToken(sampleVideoID)
	if err != nil {
		fmt.Println("Token 生成失败！")
		return
	}
	fmt.Printf("Token: %s\n\n", token)

	// 2. 解析 Token
	fmt.Println("【步骤2】解析 Token")
	header := playauth.ParseHeader(token)
	payload := playauth.ParsePayload(token)
	fmt.Printf("Header: %s\n", header)
	fmt.Printf("Payload: %s\n\n", payload)

	// 3. 验证 Token
	fmt.Println("【步骤3】验证 Token")
	isValid := playauth.VerifyToken(token)
	if isValid {
		fmt.Println("验证结果: 通过")
	} else {
		fmt.Println("验证结果: 失败")
	}
}
