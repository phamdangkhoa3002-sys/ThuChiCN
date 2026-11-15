package com.example.thuchicn;

public class item_todo {

    private int id;
    private String loai;
    private double soTien;
    private String phanLoai;
    private String ngay;
    private String gio;

    public item_todo(int id, String loai, double soTien,
                     String phanLoai, String ngay, String gio) {

        this.id = id;
        this.loai = loai;
        this.soTien = soTien;
        this.phanLoai = phanLoai;
        this.ngay = ngay;
        this.gio = gio;
    }

    public int getId() {
        return id;
    }

    public String getLoai() {
        return loai;
    }

    public double getSoTien() {
        return soTien;
    }

    public String getPhanLoai() {
        return phanLoai;
    }

    public String getNgay() {
        return ngay;
    }

    public String getGio() {
        return gio;
    }

}
