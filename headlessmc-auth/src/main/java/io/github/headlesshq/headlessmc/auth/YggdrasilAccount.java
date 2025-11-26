package io.github.headlesshq.headlessmc.auth;

import com.google.gson.JsonObject;
import lombok.Data;
import io.github.headlesshq.headlessmc.api.HasName;

@Data
public class YggdrasilAccount implements HasName {
    private final String serverUrl;
    private final String accessToken;
    private final String clientToken;
    private final String uuid;
    private final String name;
    private final String username; // 第三方登录用户名
    private final String password; // 第三方登录密码

    @Override
    public String getName() {
        return name;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "yggdrasil");
        jsonObject.addProperty("serverUrl", serverUrl);
        jsonObject.addProperty("accessToken", accessToken);
        if (clientToken != null) {
            jsonObject.addProperty("clientToken", clientToken);
        }
        jsonObject.addProperty("uuid", uuid);
        jsonObject.addProperty("name", name);
        // 保存用户名和密码
        if (username != null) {
            jsonObject.addProperty("username", username);
        }
        if (password != null) {
            jsonObject.addProperty("password", password);
        }
        return jsonObject;
    }

    public static YggdrasilAccount fromJson(JsonObject jsonObject) {
        return new YggdrasilAccount(
            jsonObject.get("serverUrl").getAsString(),
            jsonObject.get("accessToken").getAsString(),
            jsonObject.has("clientToken") ? jsonObject.get("clientToken").getAsString() : null,
            jsonObject.get("uuid").getAsString(),
            jsonObject.get("name").getAsString(),
            jsonObject.has("username") ? jsonObject.get("username").getAsString() : null,
            jsonObject.has("password") ? jsonObject.get("password").getAsString() : null
        );
    }

}

