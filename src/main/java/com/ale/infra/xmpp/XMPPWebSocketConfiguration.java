package com.ale.infra.xmpp;

import com.koushikdutta.async.http.AsyncSSLEngineConfigurator;
import com.koushikdutta.async.http.WebSocket.StringCallback;

import org.jivesoftware.smack.ConnectionConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.TrustManager;

public class XMPPWebSocketConfiguration extends ConnectionConfiguration {
	private StringCallback additionalResponseListener;
	private boolean https;
    private String file;
    private TrustManager[] tms;
    private HostnameVerifier hostVerifier;
    private AsyncSSLEngineConfigurator engineConfigurator;

    private XMPPWebSocketConfiguration(Builder builder) {
        super(builder);
        https = builder.https;
        tms = builder.trustManager;
        hostVerifier = builder.hostVerifier;
        engineConfigurator = builder.engineConfigurator;
        if (builder.file.charAt(0) != '/') {
            file = '/' + builder.file;
        } else {
            file = builder.file;
        }
    }
	
	TrustManager[] getTrustManager() {
		return tms;
	}
	
	HostnameVerifier getHostVerifier() {
		return hostVerifier;
	}
	
    public URI getURI() throws URISyntaxException
    {
        return new URI((https ? "https://" : "http://") + this.host + ":" + this.port + file);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getHost()
    {
        return host;
    }

    int getPort()
    {
        return port;
    }

    AsyncSSLEngineConfigurator getEngineConfigurator()
    {
        return engineConfigurator;
    }

    public void shutdown()
    {
        additionalResponseListener = null;
        hostVerifier = null;
    }

    public static class Builder extends ConnectionConfiguration.Builder<Builder, XMPPWebSocketConfiguration> {
        private boolean https;
        private String file;
        private TrustManager[] trustManager;
        private HostnameVerifier hostVerifier;
        private AsyncSSLEngineConfigurator engineConfigurator;

        private Builder() {
        }

        Builder setUseHttps(boolean useHttps) {
            https = useHttps;
            return getThis();
        }

        public Builder setFile(String file) {
            this.file = file;
            return getThis();
        }
        
        Builder setHostVerifier(HostnameVerifier hostVerifier) {
			this.hostVerifier = hostVerifier;
			return getThis();
		}

        Builder setTrustManager(TrustManager[] trustManager) {
			this.trustManager = trustManager;
			return getThis();
		}

        @Override
        public XMPPWebSocketConfiguration build() {
            return new XMPPWebSocketConfiguration(this);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        Builder setEngineConfigurator(AsyncSSLEngineConfigurator engineConfigurator)
        {
            this.engineConfigurator = engineConfigurator;
            return getThis();
        }
    }
}
