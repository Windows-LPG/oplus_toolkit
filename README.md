<div align="center">
  <img width="120" height="120" alt="logo" src="https://github.com/user-attachments/assets/d055944a-30cd-48d4-9df0-0a88a7813c47" />
</div>
<div align="center">
  
  <h1>OPlus Toolkit</h1>
  <p>专为 ColorOS 打造的轻量级工具箱</p>

  <!-- 徽章区域 -->
  <p>
    <!-- 1. 动态读取最新 Release 版本号 -->
    <a href="https://github.com/windows-lpg/oplus_toolkit/releases/latest">
      <img src="https://img.shields.io/github/v/release/windows-lpg/oplus_toolkit?color=orange&logo=github" alt="Release" />
    </a>
    <!-- 2. 动态读取 GitHub Stars 数 -->
    <a href="https://github.com/windows-lpg/oplus_toolkit/stargazers">
      <img src="https://img.shields.io/github/stars/windows-lpg/oplus_toolkit?style=flat&color=yellow&logo=github" alt="Stars" />
    </a>
    <!-- 3. GPLv3 协议徽章（固定写法，杜绝 invalid 翻车） -->
    <a href="https://github.com/windows-lpg/oplus_toolkit/blob/main/LICENSE">
      <img src="https://img.shields.io/badge/license-GPLv3-blue.svg" alt="License" />
    </a>
  </p>

  <p>告别复杂的终端命令与翻文件，一键了解手机的软硬件信息</p>
</div>


## ✨ 核心特性

- 📱 **屏幕面板识别**：读取系统文件，翻译型号为京东方（BOE）、三星（Samsung）、天马（Tianma）等明文厂商。
- ⚡ **内存与闪存透视**：读取devinfo的ufs，解析出闪存内存厂商。
- 🔋 **查看电池健康状况**：调用系统API，读取电池信息
- 🎨 **现代质感设计**：全界面采用 Material 3 卡片流布局，清晰呈现原始代号与解析结果。
- 📂 **规则解耦与自定义导入**：内置默认映射字典，同时支持通过系统文件选择器导入外部 `.json` 规则或添加URL联网下载JSON，方便社区更新共建。
- 🔍 **隐私卫士**：扫描系统中监控组件的运行状态，并给出卸载方案（暂未制作）
- ➕ **后续添加更多欧加专属功能**
---

## 📸 运行预览

> 
> 
> <div align="center">
  <img width="220" height="480" alt="Screenshot_2026-11-27-18-37-00-68_cc039699cecc85" src="https://github.com/user-attachments/assets/2140541a-5632-4014-9997-337ac237985d" width="10%" alt="Logo"/>

  </div>



## 📥 自定义规则格式 (rules.json)

如果你想扩展未知机型或补充物料代号，可编写自定义 JSON 文件并通过 App 内的“导入规则”按钮载入：

```json
{
  "lcd": {
    "AA607": "天马 (Tianma)",
  },
  "ddr": {
    "Samsung": "三星 (Samsung)",
  },
  "ufs": {
    "HN8T": "海力士 (SK Hynix)",
  }
}

```

---

## ✈️  联网下载规则

最终下载的文件符合上述格式即可

欢迎通过提交 Pull Request 或 Issue 分享你抓取到的新硬件代号！如果对你有用记得点一个星标支持一下！


## ⚠️ 使用前提与免责声明
---
环境要求：设备需已获取 Root 权限（支持 Magisk / KernelSU / APatch 等） 免root功能需获取shell权限（Shizuku）

免责声明：本软件仅对系统公开的信息进行读取，**不对数据的准确性做保证**，请不要借此攻击厂商，此外，本工具也不会修改任何系统分区或内核寄存器，请放心使用。如因用户设备自身 Root 行为导致的任何风险，均由使用者自行承担。

## ⭐Star History
感谢所有送出star的用户！如果觉得这个项目有用，请给个 ⭐ Star 支持一下！

<a href="https://www.star-history.com/?repos=windows-lpg%2Foplus_toolkit&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=windows-lpg/oplus_toolkit&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=windows-lpg/oplus_toolkit&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=windows-lpg/oplus_toolkit&type=date&legend=top-left" />
 </picture>
</a>

## 📄 开源许可
本项目遵循 GPL v3 开源协议。

## 致谢
* 电池检测相关实现参考自 [加加电池] https://github.com/dijia1124/plusplusbattery (MIT License)
* 免root检测闪存内存的思路参考自 [ColorOS 17 Memory Vendor] https://github.com/Wayne-u/coloros17-memory-vendor (MIT License) 实现参考朋友代码
