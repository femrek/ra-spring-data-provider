import { test, expect } from "@playwright/test";

/**
 * Smoke test to verify the test setup is working
 */
test.describe("Smoke Test", () => {
  test("should load the application", async ({ page }) => {
    // Listen for console messages
    page.on("console", (msg) => console.log("[Browser]:", msg.text()));
    page.on("pageerror", (err) => console.log("[Error]:", err.message));

    await page.goto("/");

    // Wait for the page to load
    await page.waitForLoadState("networkidle", { timeout: 30000 });

    // Check that we're on the page
    await expect(page).toHaveURL(/.*localhost:3000/);

    // Wait for React app to mount
    await page.waitForTimeout(5000);

    // Check if there's any content in the root
    const rootContent = await page.locator("#root").innerHTML();
    console.log("Root content length:", rootContent.length);
    console.log("Root content:", rootContent.substring(0, 200));

    // Check the page title at least loaded
    const title = await page.title();
    expect(title).toContain("React Admin");
  });
});
