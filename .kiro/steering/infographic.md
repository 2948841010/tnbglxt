---
inclusion: manual
---

# AntV Infographic - 信息图生成助手

基于 AntV Infographic 引擎的信息图生成工具，支持 ~200 种模板。

## 如何使用

当用户请求创建信息图时，按以下流程操作：

### Step 1: 确定任务类型

根据用户需求选择对应的参考文档：

| 任务类型 | 参考文档 | 说明 |
|---------|---------|------|
| 创建信息图 | `.shared/infographic/infographic-creator.md` | 生成完整的信息图 HTML 文件 |
| 生成语法 | `.shared/infographic/infographic-syntax-creator.md` + `.shared/infographic/references/prompt.md` | 仅生成 Infographic DSL 语法 |
| 创建数据项组件 | `.shared/infographic/infographic-item-creator.md` + `.shared/infographic/references/item-prompt.md` | 开发新的 Item 组件 |
| 创建结构组件 | `.shared/infographic/infographic-structure-creator.md` + `.shared/infographic/references/structure-prompt.md` | 开发新的 Structure 组件 |
| 更新模板目录 | `.shared/infographic/infographic-template-updater.md` | 添加新模板后更新目录 |

### Step 2: 阅读参考文档

根据任务类型，阅读对应的参考文档获取详细指导。

### Step 3: 执行任务

按照参考文档中的规范和流程完成任务。

---

## 快速参考

### 常用模板类型

- `sequence-*` - 流程/步骤/时间线
- `list-*` - 列表/要点
- `compare-*` - 对比分析
- `hierarchy-*` - 层级结构
- `chart-*` - 数据图表
- `quadrant-*` - 象限分析
- `relation-*` - 关系展示

### 基本语法结构

```plain
infographic <template-name>
data
  title 标题
  desc 描述
  items
    - label 条目
      desc 说明
      icon mdi/icon-name
theme
  palette #color1 #color2
```

### 图标资源

- Iconify: `mdi/rocket-launch`, `fa/star`, `bi/check`
- 浏览: https://icon-sets.iconify.design/

---

## 注意事项

1. **语言一致**: 用户用中文提问，输出内容也用中文
2. **阅读文档**: 执行任务前务必阅读对应的参考文档
3. **遵循规范**: 严格按照文档中的语法和代码规范
