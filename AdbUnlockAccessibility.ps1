# Cấp quyền Trợ Năng (Accessibility) và Cửa Sổ Nổi (Overlay) cho BYD Toolkit GB qua ADB
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "    BYD TOOLKIT GB - ADB PERMISSION UNLOCKER       " -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "Đảm bảo máy tính và xe BYD đang kết nối cùng một mạng WiFi / Hotspot."
$ip = Read-Host "Nhập địa chỉ IP của xe (VD: 192.168.1.5 hoặc 192.168.43.xxx)"

if ([string]::IsNullOrWhiteSpace($ip)) {
    Write-Host "Không có IP. Thoát." -ForegroundColor Red
    exit
}

# Tự động dò tìm đường dẫn adb từ PATH hoặc Android SDK
$adb = $null
$commandAdb = Get-Command adb -ErrorAction SilentlyContinue
if ($commandAdb) {
    $adb = $commandAdb.Source
} else {
    $candidatePaths = @(
        "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
        "$env:ANDROID_HOME\platform-tools\adb.exe",
        "C:\Users\Brian\AppData\Local\Android\Sdk\platform-tools\adb.exe",
        "C:\Android\Sdk\platform-tools\adb.exe",
        "C:\platform-tools\adb.exe"
    )
    foreach ($path in $candidatePaths) {
        if (-Not [string]::IsNullOrWhiteSpace($path) -and (Test-Path $path)) {
            $adb = $path
            break
        }
    }
}

if (-Not $adb -or -Not (Test-Path $adb)) {
    Write-Host "Không tìm thấy ADB trên hệ thống. Hãy cài đặt Android SDK platform-tools hoặc thêm adb vào PATH." -ForegroundColor Red
    exit
}

Write-Host "Sử dụng ADB tại: $adb" -ForegroundColor Yellow

Write-Host "`nĐang ngắt các kết nối ADB cũ..."
& $adb disconnect

Write-Host "`nĐang kết nối tới xe qua IP: $ip:5555 ..."
& $adb connect "$ip`:5555"
Start-Sleep -Seconds 2

$devices = & $adb devices
if ($devices -match "$ip") {
    Write-Host "`n[1/5] Kết nối thành công! Bắt đầu cấp quyền..." -ForegroundColor Green

    # 1. Cấp quyền Trợ Năng cho cả bản chuẩn lẫn bản debug
    Write-Host "[2/5] Đang kích hoạt Dịch Vụ Trợ Năng (Accessibility Service)..." -ForegroundColor Cyan
    & $adb shell settings put secure enabled_accessibility_services com.byd.toolkit.gb/com.byd.toolkit.gb.service.SteeringKeyAccessibilityService:com.byd.toolkit.gb.debug/com.byd.toolkit.gb.service.SteeringKeyAccessibilityService
    & $adb shell settings put secure accessibility_enabled 1

    # 2. Cấp quyền Cửa Sổ Nổi (Overlay - SYSTEM_ALERT_WINDOW)
    Write-Host "[3/5] Đang cấp quyền Cửa Sổ Nổi (Overlay)..." -ForegroundColor Cyan
    & $adb shell appops set com.byd.toolkit.gb SYSTEM_ALERT_WINDOW allow
    & $adb shell appops set com.byd.toolkit.gb.debug SYSTEM_ALERT_WINDOW allow

    # 3. Cho phép app luôn chạy ngầm không bị hệ thống kill (Doze Whitelist)
    Write-Host "[4/5] Đang tối ưu chạy ngầm (Battery Whitelist)..." -ForegroundColor Cyan
    & $adb shell dumpsys deviceidle whitelist +com.byd.toolkit.gb
    & $adb shell dumpsys deviceidle whitelist +com.byd.toolkit.gb.debug

    # 4. Cấp quyền Secure Settings
    Write-Host "[5/5] Cấp quyền ghi cài đặt an toàn (WRITE_SECURE_SETTINGS)..." -ForegroundColor Cyan
    & $adb shell pm grant com.byd.toolkit.gb android.permission.WRITE_SECURE_SETTINGS 2>$null
    & $adb shell pm grant com.byd.toolkit.gb.debug android.permission.WRITE_SECURE_SETTINGS 2>$null

    Write-Host "`n>>> ĐÃ CẤP TOÀN BỘ QUYỀN THÀNH CÔNG! <<<" -ForegroundColor Green
    Write-Host "Bây giờ bạn hãy quay lại màn hình xe và bấm [KIỂM TRA LẠI QUYỀN] hoặc [VÀO ỨNG DỤNG]." -ForegroundColor Green
} else {
    Write-Host "`nKhông thể kết nối tới xe ($ip:5555). Vui lòng kiểm tra lại:" -ForegroundColor Red
    Write-Host "1. Xe và máy tính phải kết nối cùng một mạng Wi-Fi hoặc Hotspot."
    Write-Host "2. Đã bật 'Gỡ lỗi USB' (USB Debugging) và 'Gỡ lỗi không dây' trong Tùy chọn nhà phát triển trên màn hình xe."
    Write-Host "3. Địa chỉ IP xe đã nhập chính xác."
}

Write-Host "`nNhấn phím bất kỳ để thoát..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
