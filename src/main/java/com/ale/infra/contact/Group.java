package com.ale.infra.contact;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.data_model.IMultiSelectable;
import com.ale.infra.list.ArrayItemList;
import com.ale.infra.searcher.IDisplayable;
import com.ale.util.StringsUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wilsius on 11/10/16.
 */

public class Group implements IMultiSelectable, IDisplayable {

    private String id;
    private String name;
    private String owner;
    private String comment = "";
    private Date creationDate;
    private ArrayItemList<Contact> groupMembers = new ArrayItemList<>();
    private final Set<GroupListener> m_changeListeners = new HashSet<GroupListener>();
    private boolean removable;

    public Group(boolean removable) {
        this.removable = removable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public ArrayItemList<Contact> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(ArrayItemList<Contact> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public void setRemovable(boolean removable) {
        this.removable = removable;
    }

    public boolean isRemovable() {
        return removable;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public List<String> getMembersJid() {
        List<String> membersJid = new ArrayList<>();
        for (Contact contact : groupMembers.getCopyOfDataList()) {
            if (!StringsUtil.isNullOrEmpty(contact.getImJabberId())) {
                membersJid.add(contact.getImJabberId());
            }
        }
        return membersJid;
    }

    @Override
    public String getDisplayName(String unknownNameString) {
        if( !StringsUtil.isNullOrEmpty(getName()) )
            return getName();
        return unknownNameString;
    }

    @Override
    public int getSelectableType() {
        return 0;
    }

    public interface GroupListener {
        void groupUpdate(Group updatedGroup);
    }

    public boolean isUserOwner() {
        return getOwner().equals(RainbowContext.getInfrastructure().getContactCacheMgr().getUser().getCorporateId());
    }


    public void notifyGroupUpdated() {
        synchronized (m_changeListeners) {
            for (GroupListener listener : m_changeListeners) {
                listener.groupUpdate(this);
            }
        }
    }

    public void registerChangeListener(GroupListener changeListener) {
        synchronized (m_changeListeners) {
            m_changeListeners.add(changeListener);
        }
    }

    public void unregisterChangeListener(GroupListener changeListener) {
        synchronized (m_changeListeners) {
            m_changeListeners.remove(changeListener);
        }
    }

}
