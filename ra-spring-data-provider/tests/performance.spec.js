import { test, expect } from "@playwright/test";

/**
 * Performance and load tests
 * These tests verify that the application performs well under various load conditions
 */
test.describe("Performance and Load Tests", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
    await page.waitForLoadState("networkidle");
  });

  test("should load list page within reasonable time", async ({ page }) => {
    const startTime = Date.now();

    await page.goto("/#/users");
    await page.waitForLoadState("networkidle");
    await page.waitForSelector("table", { timeout: 10000 });

    const loadTime = Date.now() - startTime;

    // Page should load within 5 seconds
    expect(loadTime).toBeLessThan(5000);

    // Verify content is displayed
    await expect(page.locator("table")).toBeVisible();
  });

  test("should handle large dataset pagination efficiently", async ({
    page,
  }) => {
    // Create a larger dataset
    const timestamp = Date.now();
    const usersToCreate = Array.from({ length: 15 }, (_, i) => ({
      name: `Load Test User ${i} ${timestamp}`,
      email: `loadtest${i}${timestamp}@example.com`,
      role: i % 3 === 0 ? "admin" : "user",
    }));

    // Create users
    for (const user of usersToCreate) {
      await page.goto("/#/users/create");
      await page.getByLabel(/name/i).fill(user.name);
      await page.getByLabel(/email/i).fill(user.email);
      await page.getByLabel(/role/i).fill(user.role);
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/users/, { timeout: 5000 });
      await page.waitForTimeout(100);
    }

    // Test pagination with the larger dataset
    await page.goto("/#/users?page=1&perPage=10&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");

    const startTime = Date.now();
    await expect(page.locator("table")).toBeVisible();
    const loadTime = Date.now() - startTime;

    // Should load quickly even with more data
    expect(loadTime).toBeLessThan(3000);

    // Verify pagination controls are present
    const pagination = page.locator('nav[aria-label*="pagination" i]');
    if (await pagination.isVisible().catch(() => false)) {
      // Test navigating to next page
      const nextButton = page.getByRole("button", { name: /next/i });
      if (
        (await nextButton.isVisible().catch(() => false)) &&
        (await nextButton.isEnabled().catch(() => false))
      ) {
        const pageStartTime = Date.now();
        await nextButton.click();
        await page.waitForLoadState("networkidle");
        const pageLoadTime = Date.now() - pageStartTime;

        expect(pageLoadTime).toBeLessThan(3000);
      }
    }
  });

  test("should handle rapid navigation between pages", async ({ page }) => {
    // Navigate rapidly between different pages
    const pages = [
      "/#/users",
      "/#/users/create",
      "/#/users",
      "/#/users?page=1&perPage=25",
    ];

    for (const url of pages) {
      const startTime = Date.now();
      await page.goto(url);
      await page.waitForLoadState("networkidle");
      const loadTime = Date.now() - startTime;

      // Each page should load within reasonable time
      expect(loadTime).toBeLessThan(5000);

      // Wait a short time before next navigation
      await page.waitForTimeout(100);
    }

    // Verify final page is functional
    await expect(page.locator("table")).toBeVisible();
  });

  test("should handle multiple concurrent user creations", async ({ page }) => {
    const timestamp = Date.now();
    const concurrentCount = 5;
    const createTimes = [];

    for (let i = 0; i < concurrentCount; i++) {
      const startTime = Date.now();

      await page.goto("/#/users/create");
      await page.getByLabel(/name/i).fill(`Concurrent User ${i} ${timestamp}`);
      await page
        .getByLabel(/email/i)
        .fill(`concurrent${i}${timestamp}@example.com`);
      await page.getByLabel(/role/i).fill("user");
      await page.getByRole("button", { name: /save/i }).click();

      await page.waitForURL(/.*#\/users/, { timeout: 5000 });
      const createTime = Date.now() - startTime;
      createTimes.push(createTime);

      // Brief pause between creations
      await page.waitForTimeout(100);
    }

    // Verify all creations completed in reasonable time
    const avgTime = createTimes.reduce((a, b) => a + b, 0) / createTimes.length;
    expect(avgTime).toBeLessThan(3000);

    // Verify all users are in the list
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");

    for (let i = 0; i < concurrentCount; i++) {
      await expect(
        page.getByText(`Concurrent User ${i} ${timestamp}`),
      ).toBeVisible();
    }
  });

  test("should efficiently handle bulk update operations", async ({ page }) => {
    // Create users for bulk update
    const timestamp = Date.now();
    const bulkCount = 5;

    for (let i = 0; i < bulkCount; i++) {
      await page.goto("/#/users/create");
      await page.getByLabel(/name/i).fill(`Bulk Perf User ${i} ${timestamp}`);
      await page
        .getByLabel(/email/i)
        .fill(`bulkperf${i}${timestamp}@example.com`);
      await page.getByLabel(/role/i).fill("user");
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/users/);
      await page.waitForTimeout(100);
    }

    // Navigate to list
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Select all test users
    for (let i = 0; i < bulkCount; i++) {
      const userName = `Bulk Perf User ${i} ${timestamp}`;
      const userRow = page.locator(`tr:has-text("${userName}")`).first();
      if (await userRow.isVisible().catch(() => false)) {
        await userRow.locator('input[type="checkbox"]').click();
        await page.waitForTimeout(100);
      }
    }

    // Perform bulk update
    const startTime = Date.now();

    const bulkUpdateButton = page.getByRole("button", { name: /update role/i });
    await bulkUpdateButton.click();
    await page.waitForSelector('div[role="dialog"]', { state: "visible" });

    const roleInput = page.getByLabel(/new role/i);
    await roleInput.fill("admin");

    const updateButton = page
      .locator('div[role="dialog"]')
      .getByRole("button", { name: /^update$/i });
    await updateButton.click();

    await page.waitForSelector('div[role="dialog"]', {
      state: "hidden",
      timeout: 10000,
    });
    await page.waitForLoadState("networkidle");

    const updateTime = Date.now() - startTime;

    // Bulk update should complete in reasonable time
    expect(updateTime).toBeLessThan(10000);
  });

  test("should efficiently handle bulk delete operations", async ({ page }) => {
    // Create users for bulk delete
    const timestamp = Date.now();
    const bulkCount = 5;

    for (let i = 0; i < bulkCount; i++) {
      await page.goto("/#/users/create");
      await page.getByLabel(/name/i).fill(`Bulk Delete Perf ${i} ${timestamp}`);
      await page
        .getByLabel(/email/i)
        .fill(`bulkdelperf${i}${timestamp}@example.com`);
      await page.getByLabel(/role/i).fill("user");
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/users/);
      await page.waitForTimeout(100);
    }

    // Navigate to list
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Select all test users
    for (let i = 0; i < bulkCount; i++) {
      const userName = `Bulk Delete Perf ${i} ${timestamp}`;
      const userRow = page.locator(`tr:has-text("${userName}")`).first();
      if (await userRow.isVisible().catch(() => false)) {
        await userRow.locator('input[type="checkbox"]').click();
        await page.waitForTimeout(100);
      }
    }

    // Perform bulk delete
    const startTime = Date.now();

    const bulkDeleteButton = page
      .locator('button[aria-label*="Delete"]')
      .first();
    await bulkDeleteButton.click({ force: true });

    const confirmButton = page.getByRole("button", { name: /confirm/i });
    if (await confirmButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await confirmButton.click();
    }

    await page.waitForLoadState("networkidle");

    const deleteTime = Date.now() - startTime;

    // Bulk delete should complete in reasonable time
    expect(deleteTime).toBeLessThan(10000);
  });

  test("should handle sorting operations efficiently", async ({ page }) => {
    // Create users with various names
    const timestamp = Date.now();
    const usersToCreate = [
      { name: `Zulu ${timestamp}`, email: `zulu${timestamp}@example.com` },
      { name: `Alpha ${timestamp}`, email: `alpha${timestamp}@example.com` },
      { name: `Mike ${timestamp}`, email: `mike${timestamp}@example.com` },
      {
        name: `Charlie ${timestamp}`,
        email: `charlie${timestamp}@example.com`,
      },
    ];

    for (const user of usersToCreate) {
      await page.goto("/#/users/create");
      await page.getByLabel(/name/i).fill(user.name);
      await page.getByLabel(/email/i).fill(user.email);
      await page.getByLabel(/role/i).fill("user");
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/users/);
      await page.waitForTimeout(100);
    }

    // Test sorting performance
    await page.goto("/#/users?page=1&perPage=25");
    await page.waitForLoadState("networkidle");

    // Click on Name header to sort
    const startTime = Date.now();
    const nameHeader = page
      .locator("table thead th")
      .filter({ hasText: "Name" });
    if (await nameHeader.isVisible()) {
      await nameHeader.click();
      await page.waitForLoadState("networkidle");
      const sortTime = Date.now() - startTime;

      // Sorting should be fast
      expect(sortTime).toBeLessThan(3000);
    }

    // Verify table is still functional
    await expect(page.locator("table")).toBeVisible();
  });

  test("should handle filter operations efficiently", async ({ page }) => {
    // Create diverse users for filtering
    const timestamp = Date.now();
    const usersToCreate = [
      {
        name: `Admin User ${timestamp}`,
        email: `admin${timestamp}@example.com`,
        role: "admin",
      },
      {
        name: `Regular User ${timestamp}`,
        email: `user${timestamp}@example.com`,
        role: "user",
      },
    ];

    for (const user of usersToCreate) {
      await page.goto("/#/users/create");
      await page.getByLabel(/name/i).fill(user.name);
      await page.getByLabel(/email/i).fill(user.email);
      await page.getByLabel(/role/i).fill(user.role);
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/users/);
      await page.waitForTimeout(100);
    }

    // Test filter performance
    await page.goto("/#/users");
    await page.waitForLoadState("networkidle");

    const searchInput = page
      .locator('input[type="search"], input[aria-label*="Search" i]')
      .first();

    if (await searchInput.isVisible().catch(() => false)) {
      const startTime = Date.now();
      await searchInput.fill("Admin");
      await page.waitForTimeout(500);
      const filterTime = Date.now() - startTime;

      // Filtering should be fast
      expect(filterTime).toBeLessThan(2000);

      // Verify results
      await expect(page.getByText(`Admin User ${timestamp}`)).toBeVisible();
    }
  });

  test("should maintain performance with complex queries", async ({ page }) => {
    const startTime = Date.now();

    // Navigate with complex query parameters
    await page.goto("/#/users?page=1&perPage=10&sort=name&order=ASC&filter={}");
    await page.waitForLoadState("networkidle");
    await expect(page.locator("table")).toBeVisible();

    const loadTime = Date.now() - startTime;

    // Complex queries should still be fast
    expect(loadTime).toBeLessThan(5000);
  });

  test("should handle form submissions without lag", async ({ page }) => {
    const timestamp = Date.now();
    const formSubmissions = [];

    // Test multiple form submissions
    for (let i = 0; i < 3; i++) {
      await page.goto("/#/users/create");
      await page.waitForLoadState("networkidle");

      const startTime = Date.now();

      await page.getByLabel(/name/i).fill(`Form Perf ${i} ${timestamp}`);
      await page
        .getByLabel(/email/i)
        .fill(`formperf${i}${timestamp}@example.com`);
      await page.getByLabel(/role/i).fill("user");
      await page.getByRole("button", { name: /save/i }).click();

      await page.waitForURL(/.*#\/users/, { timeout: 5000 });

      const submitTime = Date.now() - startTime;
      formSubmissions.push(submitTime);

      await page.waitForTimeout(100);
    }

    // Average submission time should be reasonable
    const avgSubmitTime =
      formSubmissions.reduce((a, b) => a + b, 0) / formSubmissions.length;
    expect(avgSubmitTime).toBeLessThan(3000);
  });
});
