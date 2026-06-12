package com.lifeos.metadata.token;

import com.lifeos.metadata.entity.ExternalProviderTokenEntity;
import java.util.function.Supplier;

public interface ProviderTokenService {
    String getAccessToken(String provider, Supplier<ExternalProviderTokenEntity> fetchNewToken);
}
