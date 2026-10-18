package com.mariocastro.reservas.e2e;

import com.mariocastro.reservas.e2e.support.TestDatabase;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PlaywrightHooks {

    private static final Path TRACES = Paths.get("target", "e2e-traces");
    private static final double TIMEOUT_MS = 5_000;

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    public static Page page() {
        return PAGE.get();
    }

    @Before("@ui")
    public void openBrowser() {
        TestDatabase.clean();
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(UiConfig.headless())
                .setSlowMo(UiConfig.slowMo()));
        BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBaseURL(UiConfig.baseUrl()));
        context.setDefaultTimeout(TIMEOUT_MS);
        context.tracing().start(new com.microsoft.playwright.Tracing.StartOptions()
                .setScreenshots(true).setSnapshots(true));

        PLAYWRIGHT.set(playwright);
        BROWSER.set(browser);
        CONTEXT.set(context);
        PAGE.set(context.newPage());
    }

    @After("@ui")
    public void closeBrowser(Scenario scenario) {
        try {
            if (PAGE.get() == null) return;
            if (scenario.isFailed()) {
                scenario.attach(page().screenshot(), "image/png", "fallo");
                String name = scenario.getName().replaceAll("[^\\p{L}\\p{N}]+", "_");
                CONTEXT.get().tracing().stop(new com.microsoft.playwright.Tracing.StopOptions()
                        .setPath(TRACES.resolve(name + ".zip")));
            } else {
                CONTEXT.get().tracing().stop();
            }
        } finally {
            if (CONTEXT.get() != null) CONTEXT.get().close();
            if (BROWSER.get() != null) BROWSER.get().close();
            if (PLAYWRIGHT.get() != null) PLAYWRIGHT.get().close();
            UiState.clear();
            PAGE.remove();
            CONTEXT.remove();
            BROWSER.remove();
            PLAYWRIGHT.remove();
        }
    }
}
