package com.husky.pyramid.test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.husky.pyramid.annotation.PyramidKey;
import lombok.Data;
import org.springframework.util.CollectionUtils;

public class A {

    static Integer fi = 10;

    public static void main(String[] args) throws ParseException {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        List<Integer> l2 = new ArrayList<>();
        l2 = list;
        l2.add(2);
        System.out.println(list.size());
    }

    public static void mergeSort(int[] a, int start, int end) {
        if (start < end) {
            int mid = (start + end) / 2;
            mergeSort(a, start, mid);
            mergeSort(a, mid + 1, end);
            merge(a, start, mid, end);
        }
    }

    public static void merge(int[] a, int left, int mid, int right) {
        int[] tmp = new int[a.length];
        int p1 = left, p2 = mid + 1, k = left;

        while (p1 <= mid && p2 <= right) {
            if (a[p1] <= a[p2]) {
                tmp[k++] = a[p1++];
            } else {
                tmp[k++] = a[p2++];
            }
        }

        while (p1 <= mid) {
            tmp[k++] = a[p1++];
        }
        while (p2 <= right) {
            tmp[k++] = a[p2++];
        }

        if (right + 1 - left >= 0) {
            System.arraycopy(tmp, left, a, left, right + 1 - left);
        }
    }

    @PyramidKey
    public String key() {
        return "key";
    }

    public String buildProvince(List<Org> orgs) {
        if (CollectionUtils.isEmpty(orgs)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Org org : orgs) {
            if (org == null || org.getProvinceId() == null) {
                continue;
            }
            sb.append(org.getProvinceId()).append(',');
        }
        return sb.toString();
    }

    @Data
    class Org {
        private String provinceId;
    }
}
