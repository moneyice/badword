package com.qianyitian.badword.dto;
import java.util.Set;

/**
     * {"legalContent":true, "illegalWords":[]}
     * {"legalContent":false,"illegalWords":["职业报仇","pcp气枪网","枪","气枪"]}
     */
    public  class WordFilterResponseVO {
        Set<String> illegalWords;
        String legalContent;
        boolean isLegal;

    public WordFilterResponseVO(Set<String> illegalWords) {
            this.illegalWords = illegalWords;
        }

    public  WordFilterResponseVO(String legalContent) {
            this.legalContent = legalContent;
        }

        public WordFilterResponseVO(boolean isLegal) {
            this.isLegal = isLegal;
        }

        public Boolean isLegal() {
            return isLegal;
        }

        public Set<String> getIllegalWords() {
            return illegalWords;
        }

        public String getLegalContent() {
            return legalContent;
        }
    }
