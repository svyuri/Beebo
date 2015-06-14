
package org.zarroboogs.weibo.bean;

import org.zarroboogs.weibo.db.task.AccountDao;
import org.zarroboogs.weibo.support.utils.ObjectToStringUtility;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class AccountBean implements Parcelable {

    private String access_token;
    private long expires_time;
    private boolean black_magic;
    private int navigationPosition;
    // uname // email tel
    private String uname;
    private String pwd;
    private String access_token_hack;
    private long expires_time_hack;
    // cookie
    private String cookie;
    private UserBean info;

    private String gsid;
    
    public String getGsid() {
		return gsid;
	}

	public void setGsid(String gsid) {
		this.gsid = gsid;
	}

	public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    
    public long getExpires_time_hack() {
		return expires_time_hack;
	}

	public void setExpires_time_hack(long expires_time_hack) {
		this.expires_time_hack = expires_time_hack;
	}

	public String getAccess_token_hack() {
		return access_token_hack;
	}

	public void setAccess_token_hack(String access_token_hack) {
		this.access_token_hack = access_token_hack;
	}

	public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getCookie() {
        return cookie;
    }

    public String getCookieInDB() {
        return AccountDao.getAccount(getUid()).getCookie();
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getUid() {
        return (info != null ? info.getId() : "");
    }

    public String getUsernick() {
        return (info != null ? info.getScreen_name() : "");
    }

    public String getAvatar_url() {
        return (info != null ? info.getAvatar_large() : "");
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public long getExpires_time() {
        return expires_time;
    }

    public void setExpires_time(long expires_time) {
        this.expires_time = expires_time;
    }

    public UserBean getInfo() {
        return info;
    }

    public void setInfo(UserBean info) {
        this.info = info;
    }

    public boolean isBlack_magic() {
        return /*black_magic*/true;
    }

    public void setBlack_magic(boolean black_magic) {
        this.black_magic = black_magic;
    }

    public int getNavigationPosition() {
        return navigationPosition;
    }

    public void setNavigationPosition(int navigationPosition) {
        this.navigationPosition = navigationPosition;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(access_token);
        dest.writeLong(expires_time);
        dest.writeInt(navigationPosition);
        // uname cookie
        dest.writeString(uname);
        dest.writeString(pwd);
        dest.writeString(cookie);
        dest.writeString(access_token_hack);
        dest.writeLong(expires_time_hack);
        dest.writeString(gsid);
        // end
        dest.writeBooleanArray(new boolean[] {
                this.black_magic
        });
        dest.writeParcelable(info, flags);
    }

    public static final Creator<AccountBean> CREATOR = new Creator<AccountBean>() {
        public AccountBean createFromParcel(Parcel in) {
            AccountBean accountBean = new AccountBean();
            accountBean.access_token = in.readString();
            accountBean.expires_time = in.readLong();
            accountBean.navigationPosition = in.readInt();

            // uname cookie
            accountBean.uname = in.readString();
            accountBean.pwd = in.readString();
            accountBean.cookie = in.readString();
            accountBean.access_token_hack = in.readString();
            accountBean.expires_time_hack = in.readLong();
            accountBean.gsid = in.readString();
            //
            boolean[] booleans = new boolean[1];
            in.readBooleanArray(booleans);
            accountBean.black_magic = booleans[0];

            accountBean.info = in.readParcelable(UserBean.class
                    .getClassLoader());

            return accountBean;
        }

        public AccountBean[] newArray(int size) {
            return new AccountBean[size];
        }
    };

    @Override
    public boolean equals(Object o) {

        return o instanceof AccountBean
                && !TextUtils.isEmpty(((AccountBean) o).getUid())
                && ((AccountBean) o).getUid().equalsIgnoreCase(getUid());

    }

    @Override
    public int hashCode() {
        return info.hashCode();
    }
}
