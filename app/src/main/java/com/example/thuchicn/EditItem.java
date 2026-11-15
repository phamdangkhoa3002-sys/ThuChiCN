package com.example.thuchicn;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

    public class EditItem extends AppCompatActivity {

        TextView tvMaSua;
        RadioGroup rgLoaiSua;

        EditText edtSoTienSua;
        EditText edtPhanLoaiSua;

        Button btnNgaySua;
        Button btnGioSua;
        Button btnLuuSua;

        DatabaseHelper db;

        int id;
        String ngay;
        String gio;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_transaction);

            Button btnQuayLaiSua = findViewById(R.id.btnQuayLaiSua);

            btnQuayLaiSua.setOnClickListener(v -> {
                finish();
            });

            // 1. Ánh xạ giao diện
            tvMaSua = findViewById(R.id.tvMaSua);
            rgLoaiSua = findViewById(R.id.rgLoaiSua);

            edtSoTienSua = findViewById(R.id.edtSoTienSua);
            edtPhanLoaiSua = findViewById(R.id.edtPhanLoaiSua);

            btnNgaySua = findViewById(R.id.btnNgaySua);
            btnGioSua = findViewById(R.id.btnGioSua);
            btnLuuSua = findViewById(R.id.btnLuuSua);

            // 2. Database
            db = new DatabaseHelper(this);

            // 3. Nhận dữ liệu từ Activity2
            id = getIntent().getIntExtra("id", -1);

            String loai = getIntent().getStringExtra("loai");
            double soTien = getIntent().getDoubleExtra("soTien", 0);
            String phanLoai = getIntent().getStringExtra("phanLoai");

            ngay = getIntent().getStringExtra("ngay");
            gio = getIntent().getStringExtra("gio");

            if (id == -1 || loai == null || phanLoai == null
                    || ngay == null || gio == null) {

                Toast.makeText(this,
                        "Không tìm thấy giao dịch!",
                        Toast.LENGTH_SHORT).show();

                finish();
                return;
            }

            // 4. Hiển thị dữ liệu cũ
            tvMaSua.setText("Mã giao dịch: #" + id);

            edtSoTienSua.setText(String.valueOf(soTien));
            edtPhanLoaiSua.setText(phanLoai);

            if (loai.equals("Thu")) {
                rgLoaiSua.check(R.id.rbThuSua);
            } else {
                rgLoaiSua.check(R.id.rbChiSua);
            }

            btnNgaySua.setText(ngay);
            btnGioSua.setText(gio);

            // 5. Chọn ngày mới
            btnNgaySua.setOnClickListener(v -> chonNgay());

            // 6. Chọn giờ mới
            btnGioSua.setOnClickListener(v -> chonGio());

            // 7. Lưu thay đổi
            btnLuuSua.setOnClickListener(v -> luuThayDoi());
        }

        // Chọn ngày
        private void chonNgay() {

            Calendar calendar = Calendar.getInstance();

            try {
                String[] parts = ngay.split("/");

                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int year = Integer.parseInt(parts[2]);

                calendar.set(year, month, day);

            } catch (Exception e) {
                // Giữ ngày hiện tại nếu không đọc được ngày cũ
            }

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {

                        ngay = String.format(
                                Locale.getDefault(),
                                "%02d/%02d/%04d",
                                dayOfMonth,
                                month + 1,
                                year
                        );

                        btnNgaySua.setText(ngay);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            dialog.show();
        }

        // Chọn giờ
        private void chonGio() {

            Calendar calendar = Calendar.getInstance();

            try {
                String[] parts = gio.split(":");

                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);

            } catch (Exception e) {
                // Giữ giờ hiện tại nếu không đọc được giờ cũ
            }

            TimePickerDialog dialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {

                        gio = String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                hourOfDay,
                                minute
                        );

                        btnGioSua.setText(gio);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );

            dialog.show();
        }

        // Lưu thay đổi
        private void luuThayDoi() {

            String soTienText = edtSoTienSua.getText().toString().trim();
            String phanLoai = edtPhanLoaiSua.getText().toString().trim();

            // Kiểm tra số tiền
            if (soTienText.isEmpty()) {
                edtSoTienSua.setError("Vui lòng nhập số tiền");
                edtSoTienSua.requestFocus();
                return;
            }

            double soTien;

            try {
                soTien = Double.parseDouble(soTienText);

                if (Double.isNaN(soTien) || Double.isInfinite(soTien)
                        || soTien <= 0) {
                    edtSoTienSua.setError("Số tiền phải lớn hơn 0");
                    edtSoTienSua.requestFocus();
                    return;
                }

            } catch (NumberFormatException e) {
                edtSoTienSua.setError("Số tiền không hợp lệ");
                edtSoTienSua.requestFocus();
                return;
            }

            // Kiểm tra phân loại
            if (phanLoai.isEmpty()) {
                edtPhanLoaiSua.setError("Vui lòng nhập phân loại");
                edtPhanLoaiSua.requestFocus();
                return;
            }

            // Kiểm tra loại giao dịch
            String loai;

            if (rgLoaiSua.getCheckedRadioButtonId() == R.id.rbThuSua) {
                loai = "Thu";
            } else if (rgLoaiSua.getCheckedRadioButtonId() == R.id.rbChiSua) {
                loai = "Chi";
            } else {
                Toast.makeText(this,
                        "Vui lòng chọn loại giao dịch!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật SQLite
            boolean result = db.updateTransaction(
                    id,
                    loai,
                    soTien,
                    phanLoai,
                    ngay,
                    gio
            );

            if (result) {

                Toast.makeText(this,
                        "Cập nhật giao dịch thành công!",
                        Toast.LENGTH_SHORT).show();

                finish();

            } else {

                Toast.makeText(this,
                        "Không thể cập nhật giao dịch!",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();

            if (db != null) {
                db.close();
            }
        }

    }

