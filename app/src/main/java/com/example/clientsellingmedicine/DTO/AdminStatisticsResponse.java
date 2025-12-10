package com.example.clientsellingmedicine.DTO;
public class AdminStatisticsResponse {
    private String status;
    private AdminOrderStatistics data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AdminOrderStatistics getData() {
        return data;
    }

    public void setData(AdminOrderStatistics data) {
        this.data = data;
    }
}
