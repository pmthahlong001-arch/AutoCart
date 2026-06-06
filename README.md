# AutoCart Mod - Fabric 1.21

## Cài đặt
1. Cần có **Fabric Loader 0.15+** và **Fabric API 0.102+** cho Minecraft 1.21
2. Copy file `.jar` vào thư mục `mods/`

## Compile từ source
```bash
./gradlew build
```
File `.jar` xuất hiện ở `build/libs/autocart-mod-1.0.0.jar`

## Cách dùng

| Phím | Chức năng |
|------|-----------|
| **RShift** | Mở/đóng ClickGUI |
| **Click chuột trái** vào module | Bật/tắt module |
| **Kéo header** panel | Di chuyển panel |

## AutoCart Settings

| Setting | Mô tả |
|---------|-------|
| **Mode** | `Bow` hoặc `CrossBow` — click để đổi |
| **Delay** | Delay giữa các lần bắn (ticks) — kéo slider |
| **Cart Aura Delay** | Delay của Cart Aura — kéo slider |
| **Slot** | Hotbar slot dùng để refill (1–9) — kéo slider |
| **Swap Back** | Tự swap về slot cũ sau khi bắn — checkbox |
| **Change Look** | Xoay nhân vật silent về phía target — checkbox |
| **Cart Aura** | Tự động đặt và bắn minecart — checkbox |
| **ReFill** | Chế độ refill: None/Normal/Legit — click để đổi |
| **Target** | Target: Players/Mobs/Both — click để đổi |

## Cấu trúc project
```
src/main/java/com/autocartmod/
├── client/AutoCartMod.java       ← Entrypoint, đăng ký keybind RShift
├── gui/ClickGuiScreen.java       ← Toàn bộ GUI (panels, sliders, checkboxes)
├── modules/
│   ├── Module.java               ← Base class cho mọi module
│   ├── ModuleManager.java        ← Quản lý list module
│   └── combat/AutoCart.java      ← Module AutoCart chính
└── settings/
    ├── Setting.java              ← Base setting
    ├── BooleanSetting.java       ← Checkbox
    ├── IntSetting.java           ← Slider
    └── EnumSetting.java          ← Dropdown / cycle
```
