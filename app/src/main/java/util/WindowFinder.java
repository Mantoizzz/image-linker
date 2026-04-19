package util;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import driver.Dwmapi;
import driver.User32Ex;

import java.awt.*;

/**
 * 查找 Windows 窗口并获取其物理像素边界。
 * <p>
 * 说明：
 * - FindWindow 按窗口标题查找句柄
 * - DwmGetWindowAttribute(DWMWA_EXTENDED_FRAME_BOUNDS=9) 返回物理像素坐标，
 * 不受进程 DPI 感知设置影响，是最可靠的方式
 */
public class WindowFinder {

    private static final int DWMWA_EXTENDED_FRAME_BOUNDS = 9;

    /**
     * 根据窗口标题查找窗口，返回其物理像素坐标矩形。
     */
    public static Rectangle findWindowPhysicalBounds(String title) {
        WinDef.HWND hwnd = User32Ex.INSTANCE.FindWindow(null, title);
        if (hwnd == null) {
            System.err.printf("[WindowFinder] 找不到标题为 \"%s\" 的窗口%n", title);
            return null;
        }
        return getPhysicalBounds(hwnd);
    }

    /**
     * 将窗口带到前台并还原（如果最小化了）。
     * 截图前必须调用，否则截到的是压在上面的其他窗口。
     *
     * @param title 窗口标题
     * @return 窗口句柄，找不到返回 null
     */
    public static WinDef.HWND bringToFront(String title) {
        WinDef.HWND hwnd = User32Ex.INSTANCE.FindWindow(null, title);
        if (hwnd == null) {
            System.err.printf("[WindowFinder] 找不到窗口: \"%s\"%n", title);
            return null;
        }

        // 如果窗口被最小化，先还原
        if (User32Ex.INSTANCE.IsIconic(hwnd)) {
            User32Ex.INSTANCE.ShowWindow(hwnd, WinUser.SW_RESTORE);
        }

        // 强制置顶后立即取消置顶，这是副作用最小的置前方式
        // 直接 SetForegroundWindow 在非前台进程调用时经常无效
        User32Ex.INSTANCE.SetWindowPos(
                hwnd,
                new WinDef.HWND(new com.sun.jna.Pointer(-1L)), // HWND_TOPMOST
                0, 0, 0, 0,
                WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE
        );
        User32Ex.INSTANCE.SetWindowPos(
                hwnd,
                new WinDef.HWND(new com.sun.jna.Pointer(-2L)), // HWND_NOTOPMOST
                0, 0, 0, 0,
                WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE
        );

        User32Ex.INSTANCE.SetForegroundWindow(hwnd);

        System.out.println("[WindowFinder] 窗口已置前: " + title);
        return hwnd;
    }

    /**
     * 通过句柄获取窗口物理像素边界。
     */
    public static Rectangle getPhysicalBounds(WinDef.HWND hwnd) {
        WinDef.RECT rect = new WinDef.RECT();
        int hr = Dwmapi.INSTANCE.DwmGetWindowAttribute(
                hwnd,
                DWMWA_EXTENDED_FRAME_BOUNDS,
                rect,
                16
        );

        if (hr != 0) {
            System.err.printf("[WindowFinder] DwmGetWindowAttribute 失败，HRESULT=0x%08X%n", hr);
            return null;
        }

        Rectangle physical = new Rectangle(
                rect.left,
                rect.top,
                rect.right - rect.left,
                rect.bottom - rect.top
        );

        System.out.printf("[WindowFinder] 物理坐标: x=%d y=%d w=%d h=%d%n",
                physical.x, physical.y, physical.width, physical.height);

        return physical;
    }
}