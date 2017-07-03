package com.ale.infra.datastorage;

import java.io.Serializable;

/**
 * Created by georges on 15/12/2016.
 */

public class RainbowCredentials implements Serializable {

    private static final long serialVersionUID = 789456123566448L;

    private String m_login;
    private String m_pwd;

    public RainbowCredentials(String login, String pwd) {
        m_login = login;
        m_pwd = pwd;
    }

    public void setPwd(String pwd) {
        m_pwd = pwd;
    }

    public String getPwd() {
        return m_pwd;
    }

    public String getLogin() {
        return m_login;
    }
}
