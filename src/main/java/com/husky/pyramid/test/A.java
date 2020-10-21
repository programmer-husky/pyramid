package com.husky.pyramid.test;

import com.husky.pyramid.annotation.PyramidKey;

import java.util.Random;

public class A {

	public static void main(String[] args) {
		int size = 10000;
		Random random = new Random();
		int[] data = new int[size];
		for (int i = 0; i < size; i++) {
			data[i] = random.nextInt(size);
		}
		mergeSort(data, 0, size - 1);
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
}
