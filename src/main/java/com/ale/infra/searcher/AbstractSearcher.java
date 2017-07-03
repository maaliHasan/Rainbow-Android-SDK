package com.ale.infra.searcher;

import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 21/11/2016.
 */

public abstract class AbstractSearcher {

    private static final String LOG_TAG = "AbstractSearcher";

    public AbstractSearcher() {
    }

    public List<IDisplayable> searchByName(IDisplayable[] displayableList, String query)
    {
        Log.getLogger().verbose(LOG_TAG, "searchByName: " + query);
        List<IDisplayable> displayablesFound = new ArrayList<>();

        for(IDisplayable disp : displayableList) {
            if( isMatchingQuery(disp, query)) {
                displayablesFound.add(disp);
            }
        }

        return displayablesFound;
    }

    public boolean isMatchingQuery(IDisplayable displayable, String query) {

        String queryWithoutAccents = StringUtils.stripAccents(query);
        String[] resInitials = StringsUtil.splitfromGreaterToSmaller(queryWithoutAccents);
        String displayNameAccentsStripped = StringUtils.stripAccents(displayable.getDisplayName(""));

        if( resInitials.length > 0 ) {
            List<Integer[]> positions = StringsUtil.findAllMatchingPositionOfInitials(displayNameAccentsStripped, queryWithoutAccents);

            if (positions.size() == resInitials.length) {
                //Log.getLogger().verbose(LOG_TAG, "Contact " + displayable.getDisplayName("") + " matches query; " + query);
                return true;
            }
        }

        return false;
    }

}
