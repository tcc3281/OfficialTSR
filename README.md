# OfficialTSR

## Mô tả ngắn gọn
OfficialTSR là một ứng dụng Android hỗ trợ nhận diện và hiển thị thông tin về các biển báo giao thông. Ứng dụng cung cấp các tính năng như nhận diện biển báo qua camera, hiển thị danh sách biển báo, và quản lý tài khoản người dùng.

## Yêu cầu hệ thống
- **Hệ điều hành**: Android 5.0 (API 21) trở lên.
- **Công cụ phát triển**: Android Studio Arctic Fox trở lên.
- **Thư viện cần thiết**:
  - Firebase Authentication
  - Firebase Firestore
  - Glide
  - Retrofit
  - Google Sign-In

## Cài đặt
1. **Clone repository**:
   ```bash
   git clone https://github.com/tcc3281/OfficialTSR.git
   cd OfficialTSR
   ```
2. **Mở dự án trong Android Studio**:
   - Chọn "Open an Existing Project" và điều hướng đến thư mục `OfficialTSR`.
3. **Cấu hình Firebase**:
   - Tải tệp `google-services.json` từ Firebase Console và đặt vào thư mục `app/`.
4. **Chạy ứng dụng**:
   - Kết nối thiết bị Android hoặc sử dụng trình giả lập.
   - Nhấn nút "Run" trong Android Studio.

## Cách sử dụng
1. **Đăng nhập**:
   - Sử dụng tài khoản Google để đăng nhập.
2. **Nhận diện biển báo**:
   - Truy cập mục "Camera" để nhận diện biển báo giao thông.
3. **Xem danh sách biển báo**:
   - Truy cập mục "Danh sách" để xem thông tin chi tiết về các biển báo.
4. **Cài đặt**:
   - Truy cập mục "Cài đặt" để bật chế độ tối hoặc đăng xuất.


## Cấu trúc thư mục
```
OfficialTSR/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/officialtsr/
│   │   │   │   ├── activities/         # Các Activity chính
│   │   │   │   │   ├── MainActivity.java
│   │   │   │   │   ├── CameraActivity.java
│   │   │   │   │   ├── AccountActivity.java
│   │   │   │   ├── adapters/           # Adapter cho RecyclerView
│   │   │   │   │   ├── TrafficSignAdapter.java
│   │   │   │   │   ├── SettingsAdapter.java
│   │   │   │   │   ├── AccountAdapter.java
│   │   │   │   ├── fragments/          # Các Fragment chính
│   │   │   │   │   ├── MainFragment.java
│   │   │   │   │   ├── ListFragment.java
│   │   │   │   │   ├── SettingsFragment.java
│   │   │   │   │   ├── NotificationCameraFragment.java
│   │   │   │   │   ├── TrafficSignDetailsFragment.java
│   │   │   │   │   ├── AccountFragment.java
│   │   │   ├── models/             # Các class mô hình dữ liệu
│   │   │   │   │   ├── TrafficSign.java
│   │   │   ├── utils/              # Các tiện ích
│   │   │   │   │   ├── AuthManager.java
│   │   │   │   │   ├── AnalyticsHelper.java
│   │   │   │   │   ├── ImageCompressor.java
│   │   │   ├── api/                # API và Retrofit client
│   │   │   │   │   ├── RetrofitClient.java
│   │   │   │   │   ├── TrafficSignApiService.java
│   │   │   ├── firebase/           # Firebase Firestore logic
│   │   │   │   │   ├── TrafficSignCollection.java
│   │   ├── res/
│   │   │   ├── layout/             # Các tệp giao diện XML
│   │   │   ├── drawable/           # Tài nguyên hình ảnh
│   │   │   ├── values/             # Tài nguyên chuỗi, màu sắc
├── README.md                           # Tài liệu dự án
```

## Demo
<div style="display: flex; justify-content: space-around;">
  <img src="/assets/list_demo.png" alt="Danh sách biển báo" width="45%" />
  <img src="/assets/detail_list_demo.png" alt="Chi tiết biển báo" width="45%" />
</div>

## Giấy phép
Dự án này được cấp phép theo giấy phép MIT. Xem tệp `LICENSE` để biết thêm chi tiết.

## Liên hệ
- **Email**: tcc3281@gmail.com
- **GitHub**: [GitHub Repository](https://github.com/tcc3281/OfficialTSR)
