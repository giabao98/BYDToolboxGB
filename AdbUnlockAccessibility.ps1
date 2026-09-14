# Cấp quyền Trợ Năng (Accessibility) cho BYD Toolkit GB qua ADB (WiFi)
Write-Host "--- BYD TOOLKIT GB ADB UNLOCKER ---" -ForegroundColor Cyan
Write-Host "Đảm bảo máy tính và xe BYD đang kết nối cùng một mạng WiFi."
$ip = Read-Host "Nhập địa chỉ IP của xe (VD: 192.168.1.5)"

if ([string]::IsNullOrWhiteSpace($ip)) {
    Write-Host "Không có IP. Thoát." -ForegroundColor Red
    exit
}

# Lấy đường dẫn adb từ Android SDK
$env:ANDROID_HOME = "C:\Users\Brian\AppData\Local\Android\Sdk"
$adb = "$env:ANDROID_HOME\platform-tools\adb.exe"

if (-Not (Test-Path $adb)) {
    Write-Host "Không tìm thấy ADB tại $adb. Hãy đảm bảo bạn đã cài Android SDK platform-tools." -ForegroundColor Red
    exit
}

Write-Host "`nĐang ngắt kết nối cũ..."
& $adb disconnect

Write-Host "`nĐang kết nối tới xe qua IP: $ip..."
& $adb connect "$ip`:5555"
Start-Sleep -Seconds 2

$devices = & $adb devices
if ($devices -match "$ip") {
    Write-Host "`nKết nối thành công! Đang tiến hành cấp quyền Trợ Năng ẩn..." -ForegroundColor Green
    & $adb shell settings put secure enabled_accessibility_services com.byd.toolkit.gb/com.byd.toolkit.gb.service.SteeringKeyAccessibilityService
    
    # Cho phép app luôn chạy ngầm không bị kill
    & $adb shell dumpsys deviceidle whitelist +com.byd.toolkit.gb

    Write-Host "`nĐã cấp quyền XONG! Bạn có thể kiểm tra lại trên màn hình xe." -ForegroundColor Green
} else {
    Write-Host "`nKhông thể kết nối tới xe. Vui lòng kiểm tra lại:" -ForegroundColor Red
    Write-Host "1. Xe và máy tính phải dùng chung WiFi."
    Write-Host "2. Đã bật 'Gỡ lỗi USB' (USB Debugging) và 'Gỡ lỗi không dây' trong Tùy chọn nhà phát triển trên xe."
}

Write-Host "`nNhấn phím bất kỳ để thoát..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
