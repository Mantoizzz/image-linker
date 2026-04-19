package driver;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

public interface Dwmapi extends StdCallLibrary {

    Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class);

    /**
     * @param hwnd        目标窗口句柄
     * @param dwAttribute 属性ID，9 = DWMWA_EXTENDED_FRAME_BOUNDS（物理像素边界）
     * @param pvAttribute 输出的 RECT 结构
     * @param cbAttribute RECT 的字节大小，固定传 16
     * @return 0 表示成功，非 0 为 HRESULT 错误码
     */
    int DwmGetWindowAttribute(
            WinDef.HWND hwnd,
            int dwAttribute,
            WinDef.RECT pvAttribute,
            int cbAttribute
    );
}
