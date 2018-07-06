package com.qianyitian.badword.dto;

public class ResponseVO<T> {
        boolean success = true;
        T result;

        ResponseVO fail() {
            this.success = false;
            return this;
        }

        ResponseVO result(T result) {
            this.result = result;
            return this;
        }

        public boolean isSuccess() {
            return success;
        }

        public T getResult() {
            return result;
        }
    }