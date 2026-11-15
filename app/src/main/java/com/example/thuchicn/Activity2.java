package com.example.thuchicn;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Activity2 extends AppCompatActivity {
    Button btnQuayLai;
    Toolbar toolbar2;
    EditText edtTimKiem;
    RadioGroup rgTimKiem;
    TextView tvKetQua;
    RecyclerView rvGiaoDich;

    DatabaseHelper db;
    ArrayList<item_todo> danhSach;
    TransactionAdapter adapter;

    String tuKhoa = "";
    String loaiLoc = "TatCa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        btnQuayLai = findViewById(R.id.btnQuayLai);

        btnQuayLai.setOnClickListener(v -> {
            finish();
        });
        // 1. Ánh xạ giao diện
        toolbar2 = findViewById(R.id.toolbar2);
        edtTimKiem = findViewById(R.id.edtTimKiem);
        rgTimKiem = findViewById(R.id.rgTimKiem);
        tvKetQua = findViewById(R.id.tvKetQua);
        rvGiaoDich = findViewById(R.id.rvGiaoDich);

        // 2. Toolbar
        setSupportActionBar(toolbar2);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar2.setNavigationOnClickListener(v -> finish());

        // 3. Database
        db = new DatabaseHelper(this);

        // 4. RecyclerView
        danhSach = new ArrayList<>();
        adapter = new TransactionAdapter(danhSach);

        rvGiaoDich.setLayoutManager(new LinearLayoutManager(this));
        rvGiaoDich.setAdapter(adapter);

        // 5. Tìm kiếm theo mã giao dịch
        edtTimKiem.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                tuKhoa = s.toString().trim();
                locGiaoDich();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 6. Lọc Thu / Chi
        rgTimKiem.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.rbTimThu) {
                loaiLoc = "Thu";
            } else if (checkedId == R.id.rbTimChi) {
                loaiLoc = "Chi";
            } else {
                loaiLoc = "TatCa";
            }

            locGiaoDich();
        });

        // 7. Hiển thị danh sách
        locGiaoDich();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && danhSach != null) {
            locGiaoDich();
        }
    }

    // Tìm kiếm và lọc giao dịch
    private void locGiaoDich() {

        danhSach.clear();

        Cursor cursor = db.getAllTransactions();

        if (cursor != null) {

            while (cursor.moveToNext()) {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));

                String loai = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOAI));

                double soTien = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SOTIEN));

                String phanLoai = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PHANLOAI));

                String ngay = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NGAY));

                String gio = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GIO));

                boolean dungMa = String.valueOf(id).contains(tuKhoa);

                boolean dungLoai = loaiLoc.equals("TatCa")
                        || loai.equals(loaiLoc);

                if (dungMa && dungLoai) {
                    danhSach.add(new item_todo(
                            id, loai, soTien, phanLoai, ngay, gio));
                }
            }

            cursor.close();
        }

        adapter.notifyDataSetChanged();
        tvKetQua.setText(danhSach.size() + " giao dịch");
    }

    // Adapter
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
                    "Mã giao dịch: #" + item.getId());

            if (item.getLoai().equals("Thu")) {

                holder.tvLoai.setText("Thu nhập");
                holder.tvLoai.setTextColor(0xFF219653);

                holder.tvSoTien.setTextColor(0xFF219653);
                holder.tvSoTien.setText(
                        "+ " + dinhDangTien(item.getSoTien()));

            } else {

                holder.tvLoai.setText("Chi tiêu");
                holder.tvLoai.setTextColor(0xFFE53935);

                holder.tvSoTien.setTextColor(0xFFE53935);
                holder.tvSoTien.setText(
                        "- " + dinhDangTien(item.getSoTien()));
            }

            holder.tvNgayGio.setText(
                    item.getNgay() + " - " + item.getGio());

            // Reset checkbox khi RecyclerView tái sử dụng item
            holder.cbGiaoDich.setOnCheckedChangeListener(null);
            holder.cbGiaoDich.setChecked(false);

            // Nút sửa giao dịch
            holder.btnSua.setOnClickListener(v -> {

                android.content.Intent intent = new android.content.Intent(
                        Activity2.this,
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

                new AlertDialog.Builder(Activity2.this)
                        .setTitle("Xóa giao dịch")
                        .setMessage("Bạn có chắc chắn muốn xóa giao dịch #"
                                + item.getId() + " không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {

                            if (db.deleteTransaction(item.getId())) {

                                Toast.makeText(Activity2.this,
                                        "Đã xóa giao dịch!",
                                        Toast.LENGTH_SHORT).show();

                                locGiaoDich();

                            } else {

                                Toast.makeText(Activity2.this,
                                        "Xóa giao dịch thất bại!",
                                        Toast.LENGTH_SHORT).show();
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

    // Định dạng tiền Việt Nam
    private String dinhDangTien(double soTien) {

        NumberFormat formatter = NumberFormat.getInstance(
                new Locale("vi", "VN"));

        return formatter.format(soTien) + " đ";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null) {
            db.close();
        }
    }
}