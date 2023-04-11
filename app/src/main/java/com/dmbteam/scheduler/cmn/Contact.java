package com.dmbteam.scheduler.cmn;

/**
 * Created by dobrikostadinov on 2/19/15.
 */
public class Contact {

    public enum RECORDING_MODE {
        NONE(-1), INC(0), OUT(1), ALL(2), NEXT(3);
        private final int value;

        private RECORDING_MODE(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }


    private long id;
    private String mName;
    private String mPhone;
    private String mImageThumb;

    private boolean isPressed;

    public Contact() {
        mImageThumb = "";
    }

    public Contact(int id) {
        mImageThumb = "";
        this.id = id;
    }

    public String getName() {
        return mName;
    }

    public String getPhone() {
        return mPhone;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public void setImageThumb(String imageThumb) {
        mImageThumb = imageThumb;
    }

    public String getImageThumb() {
        return mImageThumb;
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void setPressed(boolean isPressed) {
        this.isPressed = isPressed;
    }
}
