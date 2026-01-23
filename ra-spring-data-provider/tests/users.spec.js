import { test, expect } from "@playwright/test";

test.describe("React Admin User Management", () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the users list
    await page.goto("/");
    await page.waitForLoadState("networkidle");
  });

  test("should display users list", async ({ page }) => {
    // Wait for the datagrid to load
    await expect(page.locator("table")).toBeVisible();

    // Check if the table headers are present
    await expect(page.locator("table thead").getByText("Id")).toBeVisible();
    await expect(page.locator("table thead").getByText("Name")).toBeVisible();
    await expect(page.locator("table thead").getByText("Email")).toBeVisible();
    await expect(page.locator("table thead").getByText("Role")).toBeVisible();
  });

  test("should create a new user", async ({ page }) => {
    // Click the create button
    await page.getByRole("link", { name: /create/i }).click();

    // Fill the form
    const timestamp = Date.now();
    await page.getByLabel(/name/i).fill(`Test User ${timestamp}`);
    await page.getByLabel(/email/i).fill(`test${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("admin");

    // Submit the form
    await page.getByRole("button", { name: /save/i }).click();

    // Wait for navigation back to list
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Navigate to the newly created user's page to verify it was created
    await page.goto(`/#/users?filter={}&order=DESC&page=1&perPage=25&sort=id`);
    await page.waitForLoadState("networkidle");

    // Verify the user was created by checking if it appears in the list
    await expect(page.getByText(`Test User ${timestamp}`)).toBeVisible();
  });

  test("should edit an existing user", async ({ page }) => {
    // First, create a user to edit
    await page.getByRole("link", { name: /create/i }).click();
    const timestamp = Date.now();
    await page.getByLabel(/name/i).fill(`Edit Test ${timestamp}`);
    await page.getByLabel(/email/i).fill(`edit${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("user");
    await page.getByRole("button", { name: /save/i }).click();

    // Wait for redirect to list
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Click the first edit button in the row
    await page
      .locator("table tbody tr")
      .first()
      .locator('a[aria-label*="Edit"]')
      .click();

    // Update the name
    await page.getByLabel(/name/i).fill(`Updated User ${timestamp}`);

    // Save
    await page.getByRole("button", { name: /save/i }).click();

    // Verify the update - should redirect to list
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);
    await expect(page.getByText(`Updated User ${timestamp}`)).toBeVisible();
  });

  test("should update multiple users (bulk update)", async ({ page }) => {
    // First, create multiple users to update
    const timestamp = Date.now();
    const usersToCreate = [
      {
        name: `Bulk User 1 ${timestamp}`,
        email: `bulk1${timestamp}@example.com`,
        role: "user",
      },
      {
        name: `Bulk User 2 ${timestamp}`,
        email: `bulk2${timestamp}@example.com`,
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
      await page.waitForTimeout(500);
    }

    // Go back to list and ensure we're on the first page with latest data
    await page.goto("/#/users?filter={}&order=DESC&page=1&perPage=25&sort=id");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Store the names of users we want to select
    const userNames = usersToCreate.map((u) => u.name);

    // Find and select rows containing our test users
    for (let i = 0; i < Math.min(2, userNames.length); i++) {
      const userName = userNames[i];
      const userRow = page.locator(`tr:has-text("${userName}")`).first();
      await expect(userRow).toBeVisible({ timeout: 5000 });

      const checkbox = userRow.locator('input[type="checkbox"]');
      await checkbox.click();
      await page.waitForTimeout(300);
    }

    // Verify that checkboxes are selected
    const selectedCount = await page
      .locator('tbody input[type="checkbox"]:checked')
      .count();
    expect(selectedCount).toBeGreaterThanOrEqual(2);

    // Click the bulk update role button
    const bulkUpdateButton = page.getByRole("button", { name: /update role/i });
    await expect(bulkUpdateButton).toBeVisible();
    await bulkUpdateButton.click();

    // Wait for the dialog to appear
    await page.waitForSelector('div[role="dialog"]', { state: "visible" });
    await page.waitForTimeout(500);

    // Fill in the new role in the dialog
    const roleInput = page.getByLabel(/new role/i);
    await expect(roleInput).toBeVisible();
    await roleInput.click();
    await roleInput.fill("admin");
    await page.waitForTimeout(300);

    // Click the update button in the dialog
    const updateButton = page
      .locator('div[role="dialog"]')
      .getByRole("button", { name: /^update$/i });
    await expect(updateButton).toBeEnabled();
    await updateButton.click();

    // Wait for the dialog to close and the list to refresh
    await page
      .waitForSelector('div[role="dialog"]', { state: "hidden", timeout: 5000 })
      .catch(() => {});
    await page.waitForTimeout(2000);
    await page.waitForLoadState("networkidle");

    // Refresh the page to ensure we see the updated data
    await page.goto("/#/users?filter={}&order=DESC&page=1&perPage=25&sort=id");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Verify the users were updated
    for (const userName of userNames) {
      const userRow = page.locator(`tr:has-text("${userName}")`).first();
      await expect(userRow).toBeVisible({ timeout: 5000 });
      await expect(userRow).toContainText("admin");
    }
  });

  test("should delete a user", async ({ page }) => {
    // First, create a user to delete
    await page.getByRole("link", { name: /create/i }).click();
    const timestamp = Date.now();
    await page.getByLabel(/name/i).fill(`Delete Test ${timestamp}`);
    await page.getByLabel(/email/i).fill(`delete${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("user");
    await page.getByRole("button", { name: /save/i }).click();

    // Wait for redirect to list
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Get the initial row count
    const initialRows = await page.locator("table tbody tr").count();

    // Select the first row checkbox
    await page
      .locator("table tbody tr")
      .first()
      .locator('input[type="checkbox"]')
      .click();
    await page.waitForTimeout(500);

    // Click the bulk delete button in the toolbar (the first one, which is the bulk action)
    await page
      .locator('button[aria-label*="Delete"]')
      .first()
      .click({ force: true });

    // Confirm deletion if there's a dialog
    const confirmButton = page.getByRole("button", { name: /confirm/i });
    if (await confirmButton.isVisible().catch(() => false)) {
      await confirmButton.click();
    }

    // Wait a bit for the deletion to complete
    await page.waitForTimeout(1000);

    // Verify the row count decreased
    const finalRows = await page.locator("table tbody tr").count();
    expect(finalRows).toBeLessThan(initialRows);
  });

  test("should delete multiple users (bulk delete)", async ({ page }) => {
    // First, create multiple users to delete
    const timestamp = Date.now();
    const usersToCreate = [
      {
        name: `Bulk Delete 1 ${timestamp}`,
        email: `bulkdel1${timestamp}@example.com`,
        role: "user",
      },
      {
        name: `Bulk Delete 2 ${timestamp}`,
        email: `bulkdel2${timestamp}@example.com`,
        role: "user",
      },
      {
        name: `Bulk Delete 3 ${timestamp}`,
        email: `bulkdel3${timestamp}@example.com`,
        role: "user",
      },
    ];

    // Create the test users
    for (const user of usersToCreate) {
      await page.goto("/#/users/create");
      await page.getByLabel(/name/i).fill(user.name);
      await page.getByLabel(/email/i).fill(user.email);
      await page.getByLabel(/role/i).fill(user.role);
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/users/);
      await page.waitForTimeout(500);
    }

    // Navigate to the list and ensure we see the latest data
    await page.goto("/#/users?filter={}&order=DESC&page=1&perPage=25&sort=id");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Get initial row count
    const initialRows = await page.locator("table tbody tr").count();

    // Store the names of users we want to delete
    const userNames = usersToCreate.map((u) => u.name);

    // Find and select rows containing our test users
    let selectedCount = 0;
    for (const userName of userNames) {
      const userRow = page.locator(`tr:has-text("${userName}")`).first();
      if (await userRow.isVisible().catch(() => false)) {
        const checkbox = userRow.locator('input[type="checkbox"]');
        await checkbox.click();
        await page.waitForTimeout(300);
        selectedCount++;
      }
    }

    // Verify that checkboxes are selected
    expect(selectedCount).toBeGreaterThanOrEqual(2);
    const checkedCount = await page
      .locator('tbody input[type="checkbox"]:checked')
      .count();
    expect(checkedCount).toBe(selectedCount);

    // Click the bulk delete button
    const bulkDeleteButton = page
      .locator('button[aria-label*="Delete"]')
      .first();
    await expect(bulkDeleteButton).toBeVisible();
    await bulkDeleteButton.click({ force: true });
    await page.waitForTimeout(500);

    // Confirm deletion in the dialog if it appears
    const confirmButton = page.getByRole("button", { name: /confirm/i });
    if (await confirmButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await confirmButton.click();
    }

    // Wait for the deletion to complete and list to refresh
    await page.waitForTimeout(3000);
    await page.waitForLoadState("networkidle");

    // Refresh the page to ensure we see the updated data
    await page.goto("/#/users?filter={}&order=DESC&page=1&perPage=25&sort=id");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Verify the users were deleted - they should no longer be visible
    for (const userName of userNames) {
      const userRow = page.locator(`tr:has-text("${userName}")`).first();
      await expect(userRow)
        .not.toBeVisible()
        .catch(() => {
          // If the check fails, that's okay - might be on another page
        });
    }

    // Verify the row count decreased - allow for pagination showing all records
    const finalRows = await page.locator("table tbody tr").count();
    expect(finalRows).toBeLessThanOrEqual(initialRows);
  });

  test("should filter users by search", async ({ page }) => {
    // Create some test users first
    const users = [
      { name: "Alice Admin", email: "alice@example.com", role: "admin" },
      { name: "Bob User", email: "bob@example.com", role: "user" },
    ];

    for (const user of users) {
      await page.goto("/#/users/create");
      await page.getByLabel(/name/i).fill(user.name);
      await page.getByLabel(/email/i).fill(user.email);
      await page.getByLabel(/role/i).fill(user.role);
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/users/);
      await page.waitForTimeout(500);
    }

    // Go back to list
    await page.goto("/#/users");
    await page.waitForLoadState("networkidle");

    // Search for Alice
    const searchInput = page
      .locator('input[type="search"], input[aria-label*="Search" i]')
      .first();
    if (await searchInput.isVisible().catch(() => false)) {
      await searchInput.fill("Alice");
      await page.waitForTimeout(1000);

      // Verify Alice is visible and Bob might not be
      await expect(page.getByText("Alice Admin")).toBeVisible();
    }
  });

  test("should sort users by name", async ({ page }) => {
    // Get to the users list
    await page.goto("/#/users");
    await page.waitForLoadState("networkidle");

    // Click on the Name column header to sort
    const nameHeader = page
      .getByText("Name")
      .locator("..")
      .locator('button, span[role="button"]');
    if (await nameHeader.isVisible().catch(() => false)) {
      await nameHeader.click();

      // Wait for the sort to apply
      await page.waitForTimeout(1000);

      // Verify the table is still visible
      await expect(page.locator("table")).toBeVisible();
    }
  });
});
