# image-linker

适用于 Windows 平台连连看类游戏的实时辅助覆盖工具。

## 工作原理

1. 通过窗口标题定位游戏窗口
2. 截取棋盘区域截图
3. 将棋盘切割为单个格子并计算哈希
4. 运行路径查找算法，找出所有可消除的配对（最多两个折点）
5. 在游戏窗口上方绘制透明、可穿透点击的覆盖层，高亮显示可消除的连线

## 配置

运行前，需根据目标窗口修改以下两处配置：

**`Main.java`** — 设置窗口标题：
```java
private static final String WINDOW_TITLE = "Game Window";
```

**`Slicer.java`** — 设置棋盘布局（单位：像素）：
```java
public static final int ROWS = 0;        // 格子行数
public static final int COLS = 0;        // 格子列数
public static final int BOARD_X = 0;     // 棋盘在窗口内的起始 X 坐标
public static final int BOARD_Y = 0;     // 棋盘在窗口内的起始 Y 坐标
public static final int BOARD_WIDTH  = 0;
public static final int BOARD_HEIGHT = 0;
public static final int TILE_WIDTH  = 0;
public static final int TILE_HEIGHT = 0;
public static final int TILE_GAP    = 0; // 格子之间的像素间距
```

## 环境要求

- Windows（通过 JNA 调用 DWM 和 Win32 API）
- JDK 17+
- Gradle 9+

## 构建与运行

```bash
./gradlew run
```

## 项目结构

```
src/main/java/
├── Main.java                  # 入口，主循环
├── algorithm/CoreAlgorithm    # 可连接配对路径查找（最多两折）
├── entity/                    # Tile、TilePair、TileHash 记录类
├── ui/
│   ├── OverlayWindow          # 透明可穿透点击的 Swing 覆盖层
│   └── GameController         # 显示队列与刷新逻辑
└── util/
    ├── Slicer                 # 棋盘与格子裁切
    ├── TileHasher             # 格子感知哈希
    ├── BoardConverter         # 格子列表 → int[][] 棋盘
    ├── TileMapper             # 算法结果 → 实体配对
    ├── PairSelector           # 筛选不重叠的配对用于显示
    ├── ScreenshotHelper       # 支持 DPI 缩放的截图工具
    └── WindowFinder           # Win32 窗口查找与置前
```

## 算法说明

`CoreAlgorithm` 找出所有相同图案的格子对，要求连接路径最多经过两个折点且不超出棋盘边界。匹配基于感知哈希，对轻微的渲染差异有一定容错性。
