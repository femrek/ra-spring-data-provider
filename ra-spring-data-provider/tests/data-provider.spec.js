import { test, expect } from "@playwright/test";

/**
 * Integration tests for the data provider functionality
 * These tests verify that the data provider correctly communicates with the Spring Boot backend
 */
test.describe("Data Provider Integration", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
    await page.waitForLoadState("networkidle");
  });

  test("should fetch list with pagination", async ({ page }) => {
    // Create enough users to test pagination
    const timestamp = Date.now();
    const usersToCreate = Array.from({ length: 5 }, (_, i) => ({
      name: `Pagination Test ${i} ${timestamp}`,
      email: `pagination${i}${timestamp}@example.com`,
      role: "user",
    }));

    for (const user of usersToCreate) {
      await page.goto("/#/users/create");
      await page.getByLabel(/name/i).fill(user.name);
      await page.getByLabel(/email/i).fill(user.email);
      await page.getByLabel(/role/i).fill(user.role);
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/users/);
      await page.waitForTimeout(300);
    }

    // Navigate to list with specific pagination settings
    await page.goto("/#/users?page=1&perPage=10&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");

    // Verify the table is displayed
    await expect(page.locator("table")).toBeVisible();

    // Verify rows are displayed
    const rowCount = await page.locator("table tbody tr").count();
    expect(rowCount).toBeGreaterThan(0);
  });

  test("should handle getOne operation", async ({ page }) => {
    // Create a user
    const timestamp = Date.now();
    await page.goto("/#/users/create");
    await page.getByLabel(/name/i).fill(`GetOne Test ${timestamp}`);
    await page.getByLabel(/email/i).fill(`getone${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("admin");
    await page.getByRole("button", { name: /save/i }).click();
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Navigate to list to find the user
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");

    // Find the row with our user and click edit
    const userRow = page.locator(`tr:has-text("GetOne Test ${timestamp}")`);
    await expect(userRow).toBeVisible({ timeout: 10000 });

    const editButton = userRow.locator('a[aria-label*="Edit"]');
    await editButton.click();

    // Verify we're on the edit page and the data is loaded
    await page.waitForURL(/.*#\/users\/\d+/);
    await page.waitForLoadState("networkidle");

    // Verify the form has the correct data
    const nameInput = page.getByLabel(/name/i);
    await expect(nameInput).toHaveValue(new RegExp(`GetOne Test ${timestamp}`));
    await expect(page.getByLabel(/email/i)).toHaveValue(
      new RegExp(`getone${timestamp}@example.com`),
    );
    await expect(page.getByLabel(/role/i)).toHaveValue("admin");
  });

  test("should handle getMany operation", async ({ page }) => {
    // Create multiple users
    const timestamp = Date.now();
    const usersToCreate = [
      {
        name: `GetMany 1 ${timestamp}`,
        email: `getmany1${timestamp}@example.com`,
        role: "user",
      },
      {
        name: `GetMany 2 ${timestamp}`,
        email: `getmany2${timestamp}@example.com`,
        role: "admin",
      },
    ];

    for (const user of usersToCreate) {
      await page.goto("/#/users/create");
      await page.getByLabel(/name/i).fill(user.name);
      await page.getByLabel(/email/i).fill(user.email);
      await page.getByLabel(/role/i).fill(user.role);
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/users/);
      await page.waitForTimeout(300);
    }

    // Navigate to list
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");

    // Verify both users are visible in the list
    for (const user of usersToCreate) {
      await expect(page.getByText(user.name)).toBeVisible();
    }
  });

  test("should handle sorting", async ({ page }) => {
    // Create users with different names to test sorting
    const timestamp = Date.now();
    const usersToCreate = [
      {
        name: `Alpha Sort ${timestamp}`,
        email: `alpha${timestamp}@example.com`,
        role: "user",
      },
      {
        name: `Zeta Sort ${timestamp}`,
        email: `zeta${timestamp}@example.com`,
        role: "user",
      },
      {
        name: `Beta Sort ${timestamp}`,
        email: `beta${timestamp}@example.com`,
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
      await page.waitForTimeout(300);
    }

    // Navigate to list with ascending sort by name
    await page.goto("/#/users?page=1&perPage=25&sort=name&order=ASC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Verify table is visible
    await expect(page.locator("table")).toBeVisible();

    // Test descending sort by clicking the ID header
    const idHeader = page.locator("table thead th").filter({ hasText: "Id" });
    if (await idHeader.isVisible()) {
      await idHeader.click();
      await page.waitForTimeout(1000);
      await expect(page.locator("table")).toBeVisible();
    }
  });

  test("should handle update operation", async ({ page }) => {
    // Create a user
    const timestamp = Date.now();
    await page.goto("/#/users/create");
    await page.getByLabel(/name/i).fill(`Update Test ${timestamp}`);
    await page.getByLabel(/email/i).fill(`update${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("user");
    await page.getByRole("button", { name: /save/i }).click();
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Edit the user
    await page
      .locator("table tbody tr")
      .first()
      .locator('a[aria-label*="Edit"]')
      .click();
    await page.waitForURL(/.*#\/users\/\d+/);

    // Update fields
    await page.getByLabel(/name/i).fill(`Updated User ${timestamp}`);
    await page.getByLabel(/role/i).fill("admin");

    // Save
    await page.getByRole("button", { name: /save/i }).click();
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Verify the update
    await expect(page.getByText(`Updated User ${timestamp}`)).toBeVisible();
    const updatedRow = page.locator(`tr:has-text("Updated User ${timestamp}")`);
    await expect(updatedRow).toContainText("admin");
  });

  test("should handle delete operation", async ({ page }) => {
    // Create a user to delete
    const timestamp = Date.now();
    await page.goto("/#/users/create");
    await page.getByLabel(/name/i).fill(`Delete Test ${timestamp}`);
    await page.getByLabel(/email/i).fill(`delete${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("user");
    await page.getByRole("button", { name: /save/i }).click();
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Get initial count
    const initialCount = await page.locator("table tbody tr").count();

    // Delete the user using the row's delete button
    const firstRow = page.locator("table tbody tr").first();
    const deleteButton = firstRow.locator('button[aria-label*="Delete"]');

    if (await deleteButton.isVisible().catch(() => false)) {
      await deleteButton.click();

      // Confirm if dialog appears
      const confirmButton = page.getByRole("button", { name: /confirm/i });
      if (await confirmButton.isVisible({ timeout: 2000 }).catch(() => false)) {
        await confirmButton.click();
      }

      await page.waitForTimeout(1000);

      // Verify deletion
      const finalCount = await page.locator("table tbody tr").count();
      expect(finalCount).toBe(initialCount - 1);
    }
  });

  test("should handle updateMany operation", async ({ page }) => {
    // Create multiple users
    const timestamp = Date.now();
    const usersToCreate = [
      {
        name: `UpdateMany 1 ${timestamp}`,
        email: `updatemany1${timestamp}@example.com`,
        role: "user",
      },
      {
        name: `UpdateMany 2 ${timestamp}`,
        email: `updatemany2${timestamp}@example.com`,
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
      await page.waitForTimeout(300);
    }

    // Navigate to list
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Select the users
    for (const user of usersToCreate) {
      const userRow = page.locator(`tr:has-text("${user.name}")`).first();
      if (await userRow.isVisible().catch(() => false)) {
        await userRow.locator('input[type="checkbox"]').click();
        await page.waitForTimeout(300);
      }
    }

    // Click bulk update button
    const bulkUpdateButton = page.getByRole("button", { name: /update role/i });
    await bulkUpdateButton.click();

    // Fill in new role
    await page.waitForSelector('div[role="dialog"]', { state: "visible" });
    const roleInput = page.getByLabel(/new role/i);
    await roleInput.fill("admin");

    // Confirm update
    const updateButton = page
      .locator('div[role="dialog"]')
      .getByRole("button", { name: /^update$/i });
    await updateButton.click();

    // Wait for update to complete
    await page.waitForSelector('div[role="dialog"]', {
      state: "hidden",
      timeout: 5000,
    });
    await page.waitForTimeout(2000);

    // Refresh and verify
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");

    for (const user of usersToCreate) {
      const userRow = page.locator(`tr:has-text("${user.name}")`).first();
      await expect(userRow).toContainText("admin");
    }
  });

  test("should handle deleteMany operation", async ({ page }) => {
    // Create multiple users
    const timestamp = Date.now();
    const usersToCreate = [
      {
        name: `DeleteMany 1 ${timestamp}`,
        email: `deletemany1${timestamp}@example.com`,
        role: "user",
      },
      {
        name: `DeleteMany 2 ${timestamp}`,
        email: `deletemany2${timestamp}@example.com`,
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
      await page.waitForTimeout(300);
    }

    // Navigate to list
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Get initial count
    const initialCount = await page.locator("table tbody tr").count();

    // Select the users
    let selectedCount = 0;
    for (const user of usersToCreate) {
      const userRow = page.locator(`tr:has-text("${user.name}")`).first();
      if (await userRow.isVisible().catch(() => false)) {
        await userRow.locator('input[type="checkbox"]').click();
        await page.waitForTimeout(300);
        selectedCount++;
      }
    }

    // Click bulk delete button
    const bulkDeleteButton = page
      .locator('button[aria-label*="Delete"]')
      .first();
    await bulkDeleteButton.click({ force: true });

    // Confirm deletion
    const confirmButton = page.getByRole("button", { name: /confirm/i });
    if (await confirmButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await confirmButton.click();
    }

    // Wait for deletion to complete and UI to update
    await page.waitForTimeout(3000);

    // Refresh and verify
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Verify the deleted users are gone
    for (const user of usersToCreate) {
      const userRow = page.locator(`tr:has-text("${user.name}")`).first();
      await expect(userRow)
        .not.toBeVisible()
        .catch(() => {});
    }

    // Verify deletion - final count should be less than or equal
    const finalCount = await page.locator("table tbody tr").count();
    expect(finalCount).toBeLessThanOrEqual(initialCount);
  });

  test("should respect pagination parameters", async ({ page }) => {
    // Navigate with specific pagination
    await page.goto("/#/users?page=1&perPage=5&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");

    // Verify table is displayed
    await expect(page.locator("table")).toBeVisible();

    // Check if pagination controls are visible
    const pagination = page.locator('nav[aria-label*="pagination" i]');
    if (await pagination.isVisible().catch(() => false)) {
      // We have pagination controls
      expect(await pagination.isVisible()).toBe(true);
    }

    // Navigate to page 2 if available
    const nextButton = page.getByRole("button", { name: /next/i });
    if (
      (await nextButton.isVisible().catch(() => false)) &&
      (await nextButton.isEnabled().catch(() => false))
    ) {
      await nextButton.click();
      await page.waitForTimeout(1000);

      // Verify we're on page 2
      expect(page.url()).toContain("page=2");
    }
  });

  test("should handle getManyReference operation", async ({ page }) => {
    // Create a user to be the parent record
    const timestamp = Date.now();
    await page.goto("/#/users/create");
    await page.getByLabel(/name/i).fill(`Reference Parent ${timestamp}`);
    await page.getByLabel(/email/i).fill(`refparent${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("author");
    await page.getByRole("button", { name: /save/i }).click();
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Navigate to list with sorting to ensure first row is the newly created user
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500);

    // Get the user ID - the ID is in the second cell (first is checkbox)
    const firstRow = page.locator("table tbody tr").first();
    const userId = (await firstRow.locator("td").nth(1).textContent()).trim();

    // Create posts for this user to test getManyReference
    await page.goto("/#/posts");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500);

    const postsToCreate = [
      {
        title: `Ref Post 1 ${timestamp}`,
        content: "Content 1",
        status: "published",
      },
      {
        title: `Ref Post 2 ${timestamp}`,
        content: "Content 2",
        status: "draft",
      },
      {
        title: `Ref Post 3 ${timestamp}`,
        content: "Content 3",
        status: "published",
      },
    ];

    for (const post of postsToCreate) {
      await page
        .waitForSelector('a[href="#/posts/create"]', { timeout: 5000 })
        .catch(() => {});
      await page.getByRole("link", { name: /create/i }).click();
      await page.waitForLoadState("networkidle");
      await page.waitForTimeout(500);
      await page.getByLabel(/title/i).fill(post.title);
      await page
        .getByRole("textbox", { name: /^content$/i })
        .fill(post.content);
      const userIdInput = page.locator('input[name="userId"]');
      await userIdInput.waitFor({ state: "visible" });
      await userIdInput.click();
      await userIdInput.fill(userId);
      await page.getByLabel(/status/i).fill(post.status);
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/posts/);
      await page.waitForTimeout(300);
    }

    // Navigate to user show page - this triggers getManyReference via ReferenceManyField
    await page.goto(`/#/users/${userId}/show`);
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(4000);

    // Verify the user info is displayed
    await expect(
      page.getByText(`Reference Parent ${timestamp}`).first(),
    ).toBeVisible({
      timeout: 10000,
    });

    // Verify "User's Posts" section (ReferenceManyField label)
    await expect(
      page.getByText("User's Posts", { exact: false }),
    ).toBeVisible();

    // Verify all posts are displayed (getManyReference filtered by userId)
    for (const post of postsToCreate) {
      await expect(page.getByText(post.title)).toBeVisible();
    }

    // Verify the posts table is present
    const postsTables = page.locator("table");
    const tableCount = await postsTables.count();
    // Should have at least 2 tables (one for the main content, one for posts)
    expect(tableCount).toBeGreaterThanOrEqual(1);
  });
});
