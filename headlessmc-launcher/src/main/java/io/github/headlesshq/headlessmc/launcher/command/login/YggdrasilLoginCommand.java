package io.github.headlesshq.headlessmc.launcher.command.login;

import lombok.CustomLog;
import io.github.headlesshq.headlessmc.api.command.CommandException;
import io.github.headlesshq.headlessmc.api.command.line.CommandLine;
import io.github.headlesshq.headlessmc.auth.LoginContext;
import io.github.headlesshq.headlessmc.auth.YggdrasilClient;
import io.github.headlesshq.headlessmc.auth.YggdrasilSession;
import io.github.headlesshq.headlessmc.auth.YggdrasilAccount;
import io.github.headlesshq.headlessmc.launcher.Launcher;
import io.github.headlesshq.headlessmc.launcher.command.AbstractLauncherCommand;

import java.io.IOException;
import java.util.List;

@CustomLog
public class YggdrasilLoginCommand extends AbstractLauncherCommand {
    private static final String DEFAULT_SERVER = "https://littleskin.cn/api/yggdrasil";

    public YggdrasilLoginCommand(Launcher ctx) {
        super(ctx, "yggdrasil", "Login with Yggdrasil authentication server (e.g. LittleSkin)");
        args.put("login <username> [password] [--server <url>]", "Login with Yggdrasil account");
        args.put("--server <url>", "Specify Yggdrasil server URL (default: " + DEFAULT_SERVER + ")");
    }

    @Override
    public void execute(String line, String... args) throws CommandException {
        // args[0] is "yggdrasil", need to check if args[1] is "login"
        if (args.length < 2 || !args[1].equalsIgnoreCase("login")) {
            ctx.log("Usage: yggdrasil login <username> [password] [--server <url>]");
            ctx.log("Example: yggdrasil login myuser mypass");
            ctx.log("Example: yggdrasil login myuser mypass --server https://littlesk.in/api/yggdrasil");
            return;
        }

        String username = null;
        String password = null;
        String serverUrl = DEFAULT_SERVER;

        // Parse arguments (starting from args[2], because args[0]="yggdrasil", args[1]="login")
        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("--server") || args[i].equals("-s")) {
                if (i + 1 < args.length) {
                    serverUrl = args[i + 1];
                    i++; // Skip URL
                }
            } else if (username == null) {
                username = args[i];
            } else if (password == null) {
                password = args[i];
            }
        }

        if (username == null || username.isEmpty()) {
            throw new CommandException("Username is required");
        }

        // If password provided, login directly
        if (password != null && !password.isEmpty()) {
            try {
                login(username, password, serverUrl);
            } catch (Exception e) {
                throw new CommandException("Failed to login: " + e.getMessage(), e);
            }
        } else {
            // If no password provided, use interactive input
            loginWithPasswordPrompt(username, serverUrl);
        }
    }

    private void loginWithPasswordPrompt(String username, String serverUrl) {
        CommandLine clm = ctx.getCommandLine();
        String helpMessage = "Enter your password or type 'abort' to cancel the login process."
            + (clm.isHidingPasswordsSupported()
                ? ""
                : " (Your password will be visible when you type!)");
        ctx.log(helpMessage);

        boolean passwordsHiddenBefore = clm.isHidingPasswords();
        clm.setHidingPasswords(true);
        clm.setWaitingForInput(true);
        clm.setCommandContext(
            new LoginContext(ctx, clm.getCommandContext(), helpMessage) {
                @Override
                protected void onCommand(String password) {
                    try {
                        login(username, password, serverUrl);
                    } catch (Exception e) {
                        ctx.log("Failed to login: " + e.getMessage());
                    } finally {
                        returnToPreviousContext();
                        if (!passwordsHiddenBefore) {
                            clm.setHidingPasswords(false);
                        }
                        clm.setWaitingForInput(false);
                    }
                }
            });
    }

    private void login(String username, String password, String serverUrl) throws CommandException {
        logInfo("========================================");
        logInfo("Starting manual login / 开始手动登录");
        logInfo("Login server: " + serverUrl + " / 登录服务器: " + serverUrl);
        logInfo("Username: " + username + " / 用户名: " + username);
        logInfo("Validating username and password... / 正在验证用户名和密码...");
        
        try {
            // Step 1: Get profiles list without token
            YggdrasilClient client = new YggdrasilClient(serverUrl);
            List<YggdrasilSession.Profile> profiles = client.getProfiles(username, password);
            
            logInfo("Username and password validated successfully / 用户名密码验证正确");
            logInfo("Found " + profiles.size() + " profile(s) on external server / 在外置服务器找到 " + profiles.size() + " 个角色:");
            
            // Step 2: List all profiles
            int index = 1;
            for (YggdrasilSession.Profile profile : profiles) {
                logInfo("  " + index + ". " + profile.getName() + " (UUID: " + profile.getId() + ")");
                index++;
            }
            
            // Step 3: Let user select profile (保存原始用户名和密码)
            if (profiles.size() > 1) {
                selectProfileAndLogin(username, password, profiles, serverUrl);
            } else {
                // Only one profile, use it directly
                YggdrasilSession.Profile selectedProfile = profiles.get(0);
                logInfo("Selected profile: " + selectedProfile.getName() + " / 玩家已选择: " + selectedProfile.getName());
                authenticateWithProfile(selectedProfile.getName(), password, serverUrl, selectedProfile, username);
            }
            
        } catch (IOException e) {
            logError("Login failed: " + e.getMessage() + " / 登录失败: " + e.getMessage());
            if (log != null) {
                log.error("Yggdrasil login error", e);
            }
            throw new CommandException("Authentication failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logError("Unexpected error during login: " + e.getMessage() + " / 登录过程发生异常: " + e.getMessage());
            if (log != null) {
                log.error("Unexpected error during login", e);
            }
            throw new CommandException("Login failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Output to both console and log file
     * 同时输出到控制台和日志文件
     */
    private void logInfo(String message) {
        ctx.log(message);
        if (log != null) {
            log.info(message);
        }
    }
    
    /**
     * Output to both console and log file (error level)
     * 同时输出到控制台和日志文件（错误级别）
     */
    private void logError(String message) {
        ctx.log(message);
        if (log != null) {
            log.error(message);
        }
    }
    
    /**
     * Output to both console and log file (warning level)
     * 同时输出到控制台和日志文件（警告级别）
     */
    private void logWarn(String message) {
        ctx.log(message);
        if (log != null) {
            log.warn(message);
        }
    }

    private void selectProfileAndLogin(String username, String password, List<YggdrasilSession.Profile> profiles, String serverUrl) {
        String helpMessage = "Enter profile number to use (1-" + profiles.size() + ", type 'abort' to cancel) / 请输入要使用的角色编号 (1-" + profiles.size() + ", 输入 'abort' 取消):";
        logInfo(helpMessage);
        
        CommandLine clm = ctx.getCommandLine();
        clm.setWaitingForInput(true);
        // 保存原始用户名和密码到闭包中
        final String originalUsername = username;
        final String originalPassword = password;
        clm.setCommandContext(
            new LoginContext(ctx, clm.getCommandContext(), helpMessage) {
                @Override
                protected void onCommand(String input) {
                    try {
                        if (input == null || input.trim().isEmpty()) {
                            logWarn("Please select a profile number / 请选择角色编号");
                            return;
                        }
                        
                        try {
                            int selectedNumber = Integer.parseInt(input.trim());
                            // User input is display number (1-based), convert to index (0-based)
                            int selectedIndex = selectedNumber - 1;
                            if (selectedIndex >= 0 && selectedIndex < profiles.size()) {
                                YggdrasilSession.Profile selected = profiles.get(selectedIndex);
                                logInfo("Selected profile: " + selected.getName() + " / 玩家已选择: " + selected.getName());
                                // Use selected profile name as username to request token, but save original username
                                authenticateWithProfile(selected.getName(), originalPassword, serverUrl, selected, originalUsername);
                            } else {
                                logWarn("Invalid number " + selectedNumber + ", please select 1-" + profiles.size() + " / 无效的编号 " + selectedNumber + "，请选择 1-" + profiles.size());
                            }
                        } catch (NumberFormatException e) {
                            logWarn("Invalid input, please enter a number / 无效的输入，请输入数字");
                        }
                    } finally {
                        returnToPreviousContext();
                        clm.setWaitingForInput(false);
                    }
                }
            });
    }
    
    /**
     * Authenticate with selected profile name and password
     * 使用选择的角色名和密码进行认证
     * @param profileName 选择的角色名（用于请求token）
     * @param password 密码
     * @param serverUrl 服务器URL
     * @param profile 角色信息
     * @param originalUsername 原始用户名（用于保存到账户）
     */
    private void authenticateWithProfile(String profileName, String password, String serverUrl, YggdrasilSession.Profile profile, String originalUsername) {
        try {
            logInfo("Requesting accessToken with profile name: " + profileName + " / 使用角色名请求 accessToken: " + profileName);
            
            YggdrasilClient client = new YggdrasilClient(serverUrl);
            // Use profile name as username to request token
            YggdrasilSession session = client.authenticate(profileName, password);
            
            logInfo("AccessToken received successfully / AccessToken 获取成功");
            createAndAddAccount(serverUrl, session, profile, originalUsername, password);
        } catch (Exception e) {
            logError("Failed to authenticate with profile: " + e.getMessage() + " / 使用角色认证失败: " + e.getMessage());
            if (log != null) {
                log.error("Authentication error", e);
            }
        }
    }

    /**
     * Create and add Yggdrasil account with username and password
     * 创建并添加 Yggdrasil 账户，包含用户名和密码
     * 覆盖模式：如果已存在同名账户，将完全覆盖旧账户（包括用户名和密码）
     */
    private void createAndAddAccount(String serverUrl, YggdrasilSession session, YggdrasilSession.Profile profile, String username, String password) {
        String accessToken = session.getAccessToken();
        String maskedToken = maskToken(accessToken);
        
        logInfo("Received accessToken: " + maskedToken + " / 请求到的 accessToken: " + maskedToken);
        
        // 覆盖模式：创建新账户时使用新的用户名和密码，完全替换旧账户
        YggdrasilAccount account = new YggdrasilAccount(
            serverUrl,
            accessToken,
            session.getClientToken(),
            profile.getId(),
            profile.getName(),
            username,  // 保存新的原始用户名（覆盖模式，不保留旧用户名）
            password    // 保存新密码（覆盖模式，不保留旧密码）
        );
        
        // addYggdrasilAccount 会自动移除同名旧账户，然后添加新账户
        ctx.getAccountManager().addYggdrasilAccount(account);
        logInfo("Login successful! Account: " + account.getName() + " / 登录成功！账户: " + account.getName());
        logInfo("Account overwritten in overwrite mode / 账户已以覆盖模式保存");
        logInfo("Username and password saved to .accounts.json / 用户名和密码已保存到 .accounts.json");
        logInfo("========================================");
    }
    
    /**
     * Masks token by showing only first 10 and last 10 characters, replacing middle with *
     * 对 token 进行打码处理，只显示前10位和后10位，中间用*代替
     * @param token Original token / 原始 token
     * @return Masked token / 打码后的 token
     */
    private String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return "null";
        }
        
        int length = token.length();
        if (length <= 20) {
            // If token length <= 20, replace all with *
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append("*");
            }
            return sb.toString();
        }
        
        // First 10 chars + middle * + last 10 chars
        String prefix = token.substring(0, 10);
        String suffix = token.substring(length - 10);
        int middleLength = length - 20;
        StringBuilder middle = new StringBuilder();
        for (int i = 0; i < middleLength; i++) {
            middle.append("*");
        }
        
        return prefix + middle.toString() + suffix;
    }
}

