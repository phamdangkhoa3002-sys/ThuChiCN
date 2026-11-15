
package com.example.thuchicn;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    RadioGroup rgLoaiGiaoDich;

    EditText edtSoTien;
    EditText edtPhanLoai;

    Button btnNgay;
    Button btnGio;
    Button btnLuu;

    TextView tvTongThu;
    TextView tvTongChi;

    RecyclerView rvGiaoDich;

    DatabaseHelper db;

    ArrayList<item_todo> danhSach;
    TransactionAdapter adapter;

    String ngay;
    String gio;

    NumberFormat dinhDangTien;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Xử lý khoảng cách với thanh trạng thái
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // 1. Ánh xạ giao diện
        rgLoaiGiaoDich = findViewById(R.id.rgLoaiGiaoDich);

        edtSoTien = findViewById(R.id.edtSoTien);
        edtPhanLoai = findViewById(R.id.edtPhanLoai);

        btnNgay = findViewById(R.id.btnNgay);
        btnGio = findViewById(R.id.btnGio);
        btnLuu = findViewById(R.id.btnLuu);

        tvTongThu = findViewById(R.id.tvTongThu);
        tvTongChi = findViewById(R.id.tvTongChi);

        rvGiaoDich = findViewById(R.id.rvGiaoDich);

        // 2. Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 3. Database
        db = new DatabaseHelper(this);

        dinhDangTien = NumberFormat.getInstance(
                new Locale("vi", "VN")
        );

        // 4. Thiết lập RecyclerView
        danhSach = new ArrayList<>();
        adapter = new TransactionAdapter(danhSach);

        rvGiaoDich.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvGiaoDich.setAdapter(adapter);

        // Không cuộn riêng RecyclerView bên trong ScrollView
        rvGiaoDich.setNestedScrollingEnabled(false);

        // 5. Lấy ngày giờ hiện tại
        Calendar calendar = Calendar.getInstance();

        ngay = String.format(
                Locale.getDefault(),
                "%02d/%02d/%04d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
        );

        gio = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
        );

        btnNgay.setText(ngay);
        btnGio.setText(gio);

        // 6. Chọn ngày và giờ
        btnNgay.setOnClickListener(v -> chonNgay());
        btnGio.setOnClickListener(v -> chonGio());

        // 7. Lưu giao dịch
        btnLuu.setOnClickListener(v -> luuGiaoDich());

        // 8. Hiển thị danh sách và tổng tiền
        capNhatDanhSach();
        capNhatTongTien();
    }

    // Chọn ngày giao dịch
    private void chonNgay() {

        Calendar calendar = Calendar.getInstance();

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

                    btnNgay.setText(ngay);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    // Chọn giờ giao dịch
    private void chonGio() {

        Calendar calendar = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {

                    gio = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hourOfDay,
                            minute
                    );

                    btnGio.setText(gio);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        dialog.show();
    }

    // Lưu giao dịch vào SQLite
    private void luuGiaoDich() {

        String soTienText = edtSoTien.getText().toString().trim();
        String phanLoai = edtPhanLoai.getText().toString().trim();

        if (soTienText.isEmpty()) {
            edtSoTien.setError("Vui lòng nhập số tiền");
            edtSoTien.requestFocus();
            return;
        }

        double soTien;

        try {
            soTien = Double.parseDouble(soTienText);

            if (Double.isNaN(soTien) || Double.isInfinite(soTien)
                    || soTien <= 0) {

                edtSoTien.setError("Số tiền phải lớn hơn 0");
                edtSoTien.requestFocus();
                return;
            }

        } catch (NumberFormatException e) {
            edtSoTien.setError("Số tiền không hợp lệ");
            edtSoTien.requestFocus();
            return;
        }

        if (phanLoai.isEmpty()) {
            edtPhanLoai.setError("Vui lòng nhập phân loại");
            edtPhanLoai.requestFocus();
            return;
        }

        String loai;

        if (rgLoaiGiaoDich.getCheckedRadioButtonId() == R.id.rbThu) {
            loai = "Thu";

        } else if (rgLoaiGiaoDich.getCheckedRadioButtonId() == R.id.rbChi) {
            loai = "Chi";

        } else {
            Toast.makeText(
                    this,
                    "Vui lòng chọn loại giao dịch!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        boolean result = db.insertTransaction(
                loai,
                soTien,
                phanLoai,
                ngay,
                gio
        );

        if (result) {

            Toast.makeText(
                    this,
                    "Lưu giao dịch thành công!",
                    Toast.LENGTH_SHORT
            ).show();

            edtSoTien.setText("");
            edtPhanLoai.setText("");

            // Cập nhật danh sách và tổng tiền ngay lập tức
            capNhatDanhSach();
            capNhatTongTien();

        } else {

            Toast.makeText(
                    this,
                    "Không thể lưu giao dịch!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Lấy danh sách giao dịch từ SQLite
    private void capNhatDanhSach() {

        danhSach.clear();

        Cursor cursor = db.getAllTransactions();

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

                danhSach.add(new item_todo(
                        id, loai, soTien, phanLoai, ngay, gio
                ));
            }

            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    // Cập nhật tổng thu và tổng chi
    private void capNhatTongTien() {

        double tongThu = db.getTotalIncome();
        double tongChi = db.getTotalExpense();

        tvTongThu.setText(
                "Tổng thu: " + dinhDangTien.format(tongThu) + " VNĐ"
        );

        tvTongChi.setText(
                "Tổng chi: " + dinhDangTien.format(tongChi) + " VNĐ"
        );
    }

    // Adapter cho danh sách giao dịch
    private class TransactionAdapter
            extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

        private final ArrayList<item_todo> list;

        public TransactionAdapter(ArrayList<item_todo> list) {
            this.list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recycler, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            item_todo item = list.get(position);

            holder.tvPhanLoai.setText(item.getPhanLoai());

            holder.tvMaGiaoDich.setText(
                    "Mã giao dịch: #" + item.getId()
            );

            if (item.getLoai().equals("Thu")) {

                holder.tvLoai.setText("Thu nhập");
                holder.tvLoai.setTextColor(0xFF219653);

                holder.tvSoTien.setTextColor(0xFF219653);
                holder.tvSoTien.setText(
                        "+ " + dinhDangTien.format(item.getSoTien()) + " đ"
                );

            } else {

                holder.tvLoai.setText("Chi tiêu");
                holder.tvLoai.setTextColor(0xFFE53935);

                holder.tvSoTien.setTextColor(0xFFE53935);
                holder.tvSoTien.setText(
                        "- " + dinhDangTien.format(item.getSoTien()) + " đ"
                );
            }

            holder.tvNgayGio.setText(
                    item.getNgay() + " - " + item.getGio()
            );

            // Reset checkbox khi RecyclerView tái sử dụng item
            holder.cbGiaoDich.setOnCheckedChangeListener(null);
            holder.cbGiaoDich.setChecked(false);

            // Nút sửa
            holder.btnSua.setOnClickListener(v -> {

                Intent intent = new Intent(
                        MainActivity.this,
                        EditItem.class
                );

                intent.putExtra("id", item.getId());
                intent.putExtra("loai", item.getLoai());
                intent.putExtra("soTien", item.getSoTien());
                intent.putExtra("phanLoai", item.getPhanLoai());
                intent.putExtra("ngay", item.getNgay());
                intent.putExtra("gio", item.getGio());

                startActivity(intent);
            });

            // Nút xóa
            holder.btnXoa.setOnClickListener(v -> {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Xóa giao dịch")
                        .setMessage(
                                "Bạn có chắc chắn muốn xóa giao dịch #"
                                        + item.getId() + " không?"
                        )
                        .setPositiveButton("Xóa", (dialog, which) -> {

                            if (db.deleteTransaction(item.getId())) {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Đã xóa giao dịch!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                capNhatDanhSach();
                                capNhatTongTien();

                            } else {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Xóa giao dịch thất bại!",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        // ViewHolder
        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvPhanLoai, tvMaGiaoDich;
            TextView tvLoai, tvNgayGio, tvSoTien;

            CheckBox cbGiaoDich;
            ImageButton btnSua, btnXoa;

            public ViewHolder(View itemView) {
                super(itemView);

                tvPhanLoai = itemView.findViewById(R.id.tvPhanLoai);
                tvMaGiaoDich = itemView.findViewById(R.id.tvMaGiaoDich);
                tvLoai = itemView.findViewById(R.id.tvLoai);
                tvNgayGio = itemView.findViewById(R.id.tvNgayGio);
                tvSoTien = itemView.findViewById(R.id.tvSoTien);

                cbGiaoDich = itemView.findViewById(R.id.cbGiaoDich);
                btnSua = itemView.findViewById(R.id.btnSua);
                btnXoa = itemView.findViewById(R.id.btnXoa);
            }
        }
    }

    // Tạo menu trên Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    // Xử lý menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menuTimKiem) {

            startActivity(new Intent(
                    MainActivity.this,
                    Activity2.class
            ));

            return true;
        }

        if (id == R.id.menuThongKe) {

            startActivity(new Intent(
                    MainActivity.this,
                    Activity3.class
            ));

            return true;
        }

        if (id == R.id.menuThoat) {

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Thoát ứng dụng")
                    .setMessage("Bạn có chắc chắn muốn thoát ứng dụng không?")
                    .setPositiveButton("Có", (dialog, which) -> {

                        Toast.makeText(
                                MainActivity.this,
                                "Đã thoát app",
                                Toast.LENGTH_SHORT
                        ).show();

                        finishAffinity();
                    })
                    .setNegativeButton("Không", null)
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // Cập nhật danh sách khi quay lại MainActivity
    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && danhSach != null && adapter != null) {
            capNhatDanhSach();
            capNhatTongTien();
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
