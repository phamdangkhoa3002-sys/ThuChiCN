package com.example.thuchicn;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class Activity3 extends AppCompatActivity {

    EditText edtThang, edtNam;

    Button btnThongKe;
    Button btnQuayLaiThongKe;
    TextView tvThongKeThu;
    TextView tvThongKeChi;
    TextView tvSoDu;
    TextView tvSoGiaoDich;

    LinearLayout layoutDanhSachThongKe;

    DatabaseHelper db;

    NumberFormat dinhDangTien;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);

        btnQuayLaiThongKe = findViewById(R.id.btnQuayLaiThongKe);

        btnQuayLaiThongKe.setOnClickListener(v -> {
            finish();
        });
        // 1. Ánh xạ giao diện
        edtThang = findViewById(R.id.edtThang);
        edtNam = findViewById(R.id.edtNam);

        btnThongKe = findViewById(R.id.btnThongKe);

        tvThongKeThu = findViewById(R.id.tvThongKeThu);
        tvThongKeChi = findViewById(R.id.tvThongKeChi);
        tvSoDu = findViewById(R.id.tvSoDu);
        tvSoGiaoDich = findViewById(R.id.tvSoGiaoDich);

        layoutDanhSachThongKe =
                findViewById(R.id.layoutDanhSachThongKe);

        // 2. Khởi tạo database
        db = new DatabaseHelper(this);

        dinhDangTien = NumberFormat.getInstance(
                new Locale("vi", "VN")
        );

        // 3. Lấy tháng và năm hiện tại
        Calendar calendar = Calendar.getInstance();

        edtThang.setText(String.valueOf(
                calendar.get(Calendar.MONTH) + 1
        ));

        edtNam.setText(String.valueOf(
                calendar.get(Calendar.YEAR)
        ));

        // 4. Xem thống kê
        btnThongKe.setOnClickListener(v -> thongKeGiaoDich());

        // 5. Hiển thị thống kê ban đầu
        thongKeGiaoDich();
    }

    private void thongKeGiaoDich() {

        String thang = edtThang.getText().toString().trim();
        String nam = edtNam.getText().toString().trim();

        // Kiểm tra tháng và năm
        if (thang.isEmpty()) {
            edtThang.setError("Vui lòng nhập tháng");
            edtThang.requestFocus();
            return;
        }

        if (nam.isEmpty()) {
            edtNam.setError("Vui lòng nhập năm");
            edtNam.requestFocus();
            return;
        }

        int thangSo;
        int namSo;

        try {
            thangSo = Integer.parseInt(thang);
            namSo = Integer.parseInt(nam);

        } catch (NumberFormatException e) {
            ToastThongBao("Tháng hoặc năm không hợp lệ!");
            return;
        }

        if (thangSo < 1 || thangSo > 12) {
            edtThang.setError("Tháng phải từ 1 đến 12");
            edtThang.requestFocus();
            return;
        }

        if (namSo < 1900 || namSo > 9999) {
            edtNam.setError("Năm không hợp lệ");
            edtNam.requestFocus();
            return;
        }

        // Chuẩn hóa tháng thành 2 chữ số
        thang = String.format(
                Locale.getDefault(),
                "%02d",
                thangSo
        );

        // Lấy dữ liệu từ SQLite
        Cursor cursor = db.getTransactionsByMonthYear(
                thang,
                String.valueOf(namSo)
        );

        double tongThu = 0;
        double tongChi = 0;
        int soGiaoDich = 0;

        // Xóa danh sách cũ
        layoutDanhSachThongKe.removeAllViews();

        if (cursor != null) {

            while (cursor.moveToNext()) {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_ID
                        )
                );

                String loai = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_LOAI
                        )
                );

                double soTien = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_SOTIEN
                        )
                );

                String phanLoai = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_PHANLOAI
                        )
                );

                String ngay = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_NGAY
                        )
                );

                String gio = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.COL_GIO
                        )
                );

                // Tính tổng thu và chi
                if ("Thu".equals(loai)) {
                    tongThu += soTien;
                } else if ("Chi".equals(loai)) {
                    tongChi += soTien;
                }

                soGiaoDich++;

                // Hiển thị từng giao dịch
                themGiaoDichVaoDanhSach(
                        id,
                        loai,
                        soTien,
                        phanLoai,
                        ngay,
                        gio
                );
            }

            cursor.close();
        }

        // Tính số dư
        double soDu = tongThu - tongChi;

        // Hiển thị kết quả
        tvThongKeThu.setText(
                "Tổng thu nhập: " +
                        dinhDangTien.format(tongThu) + " VNĐ"
        );

        tvThongKeChi.setText(
                "Tổng chi tiêu: " +
                        dinhDangTien.format(tongChi) + " VNĐ"
        );

        tvSoDu.setText(
                "Số dư: " +
                        dinhDangTien.format(soDu) + " VNĐ"
        );

        tvSoGiaoDich.setText(
                "Tổng số giao dịch: " + soGiaoDich
        );

        if (soGiaoDich == 0) {
            themThongBaoKhongCoGiaoDich();
        }
    }

    // Thêm một giao dịch vào danh sách thống kê
    private void themGiaoDichVaoDanhSach(
            int id,
            String loai,
            double soTien,
            String phanLoai,
            String ngay,
            String gio) {

        LinearLayout item = new LinearLayout(this);

        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(16, 14, 16, 14);
        item.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams itemParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        itemParams.setMargins(0, 0, 0, 10);

        item.setLayoutParams(itemParams);

        // Mã và phân loại
        TextView tvTen = new TextView(this);

        tvTen.setText(
                "#" + id + " - " + phanLoai
        );

        tvTen.setTextSize(17);
        tvTen.setTypeface(null, Typeface.BOLD);
        tvTen.setTextColor(Color.rgb(38, 59, 99));

        // Loại giao dịch
        TextView tvLoai = new TextView(this);

        tvLoai.setText(
                "Loại: " +
                        ("Thu".equals(loai) ? "Thu nhập" : "Chi tiêu")
        );

        tvLoai.setTextSize(14);

        if ("Thu".equals(loai)) {
            tvLoai.setTextColor(Color.rgb(38, 155, 98));
        } else {
            tvLoai.setTextColor(Color.rgb(208, 68, 68));
        }

        // Ngày và giờ
        TextView tvNgayGio = new TextView(this);

        tvNgayGio.setText(
                "Ngày: " + ngay + " | Giờ: " + gio
        );

        tvNgayGio.setTextSize(13);
        tvNgayGio.setTextColor(Color.GRAY);

        // Số tiền
        TextView tvTien = new TextView(this);

        tvTien.setText(
                dinhDangTien.format(soTien) + " VNĐ"
        );

        tvTien.setTextSize(16);
        tvTien.setTypeface(null, Typeface.BOLD);
        tvTien.setGravity(Gravity.END);

        if ("Thu".equals(loai)) {
            tvTien.setTextColor(Color.rgb(38, 155, 98));
        } else {
            tvTien.setTextColor(Color.rgb(208, 68, 68));
        }

        item.addView(tvTen);
        item.addView(tvLoai);
        item.addView(tvNgayGio);
        item.addView(tvTien);

        layoutDanhSachThongKe.addView(item);
    }

    // Thông báo khi không có giao dịch
    private void themThongBaoKhongCoGiaoDich() {

        TextView tvTrong = new TextView(this);

        tvTrong.setText(
                "Không có giao dịch trong tháng này."
        );

        tvTrong.setTextSize(15);
        tvTrong.setTextColor(Color.GRAY);
        tvTrong.setGravity(Gravity.CENTER);
        tvTrong.setPadding(16, 30, 16, 30);

        layoutDanhSachThongKe.addView(tvTrong);
    }

    private void ToastThongBao(String noiDung) {
        android.widget.Toast.makeText(
                this,
                noiDung,
                android.widget.Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null) {
            db.close();
        }
    }
}