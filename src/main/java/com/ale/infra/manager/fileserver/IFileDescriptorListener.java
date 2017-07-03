package com.ale.infra.manager.fileserver;

/**
 * Created by georges on 13/02/2017.
 */
public interface IFileDescriptorListener {

    void onFileDescriptorUpdated(RainbowFileDescriptor fileDescriptor);
}
