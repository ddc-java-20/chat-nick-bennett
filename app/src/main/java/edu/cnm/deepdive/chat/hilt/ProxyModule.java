package edu.cnm.deepdive.chat.hilt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class ProxyModule {

  @Inject
  ProxyModule() {}

  @Provides
  @Singleton
  Gson provideGson() {
    return new GsonBuilder()
    // TODO: 2025-03-12 Register type adapters as necessary (e.g., for UUID, Instant).
        .excludeFieldsWithoutExposeAnnotation()
        .create();
  }

}
