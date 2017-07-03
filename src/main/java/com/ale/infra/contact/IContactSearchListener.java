package com.ale.infra.contact;

import java.util.List;

public interface IContactSearchListener {
    void searchStarted();

    void searchFinished(List<DirectoryContact> contactsFounded );

    void searchError();
}
