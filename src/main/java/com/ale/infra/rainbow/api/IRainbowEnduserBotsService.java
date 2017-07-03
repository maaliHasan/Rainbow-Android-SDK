package com.ale.infra.rainbow.api;

import com.ale.infra.http.adapter.concurrent.IAsyncServiceResultCallback;
import com.ale.infra.proxy.EnduserBots.GetAllBotsResponse;
import com.ale.infra.proxy.EnduserBots.GetBotDataResponse;

/**
 * Created by wilsius on 08/08/16.
 */
public interface IRainbowEnduserBotsService extends IRainbowService {

    void getAllBots(int limit, int offset, IAsyncServiceResultCallback<GetAllBotsResponse> callback);
    void getBotData(String botId, IAsyncServiceResultCallback<GetBotDataResponse> callback);

}
