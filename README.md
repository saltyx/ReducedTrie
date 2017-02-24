# 一种字典树的高效实现

## 概要

- 根据《An Efficient Implementation of Trie Structures》 论文实现

## 和双数组字典树的不同
- 增加tail数组存储非前缀部分
- base check数组更加精简
- [详细请看博客，点击这里](https://saltyx.github.io/2017/02/20/An-Efficient-Implementation-of-Trie-Structures/)

## 实现
- 字典树构建
- 根据前缀查找单词
- 存储到文件(save)和从文件加载(reload)

## 项目结构
- _data文件夹_ 存储字典树的四个文件(*.bc, *tail, *index, *list)
- _src文件夹_ 字典树源程序
- _test文件夹_ 测试字典

