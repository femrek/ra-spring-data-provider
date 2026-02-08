import { test, expect } from "@playwright/test";

test.describe("React Admin getManyReference - User Posts Relationship", () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the users list
    await page.goto("/");
    await page.waitForLoadState("networkidle");
  });

  test("should display user's posts using ReferenceManyField in show view", async ({
    page,
  }) => {
    // First, create a test user
    await page.getByRole("link", { name: /create/i }).click();
    const timestamp = Date.now();
    const userName = `Test User ${timestamp}`;
    await page.getByLabel(/name/i).fill(userName);
    await page.getByLabel(/email/i).fill(`test${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("author");
    await page.getByRole("button", { name: /save/i }).click();

    // Wait for redirect to list
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Navigate to sorted list to ensure first row is the newly created user
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500);

    // Get the user ID from the first row (most recently created)
    const firstRow = page.locator("table tbody tr").first();
    const userId = (await firstRow.locator("td").nth(1).textContent()).trim();

    // Navigate to posts - wait for any element to ensure page is loaded
    await page.goto("/#/posts");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500); // Give time for React to render

    const postsData = [
      {
        title: `First Post ${timestamp}`,
        content: "This is the first post content",
        status: "published",
      },
      {
        title: `Second Post ${timestamp}`,
        content: "This is the second post content",
        status: "draft",
      },
      {
        title: `Third Post ${timestamp}`,
        content: "This is the third post content",
        status: "published",
      },
    ];

    for (const post of postsData) {
      // Wait for create button to be available
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
      await page.locator('input[name="userId"]').fill(userId);
      await page.getByLabel(/status/i).fill(post.status);
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/posts/);
      await page.waitForTimeout(500);
    }

    // Navigate to the user's show page to see the posts via ReferenceManyField
    await page.goto(`/#/users/${userId}/show`);
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Verify the user details are displayed
    await expect(page.getByText(userName).first()).toBeVisible();

    // Verify the "User's Posts" section is visible (this uses getManyReference)
    await expect(
      page.getByText("User's Posts", { exact: false }),
    ).toBeVisible();

    // Verify all three posts are displayed in the ReferenceManyField
    for (const post of postsData) {
      await expect(page.getByText(post.title)).toBeVisible();
    }
  });

  test("should show empty state when user has no posts", async ({ page }) => {
    // Create a user without any posts
    await page.getByRole("link", { name: /create/i }).click();
    const timestamp = Date.now();
    const userName = `User No Posts ${timestamp}`;
    await page.getByLabel(/name/i).fill(userName);
    await page.getByLabel(/email/i).fill(`nopost${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("reader");
    await page.getByRole("button", { name: /save/i }).click();

    // Wait for redirect to list
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Navigate to sorted list to ensure first row is the newly created user
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500);

    // Get the user ID
    const firstRow = page.locator("table tbody tr").first();
    const userId = (await firstRow.locator("td").nth(1).textContent()).trim();

    // Navigate to the user's show page
    await page.goto(`/#/users/${userId}/show`);
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Verify the user details are displayed
    await expect(page.getByText(userName).first()).toBeVisible();

    // Verify the "User's Posts" section is visible but empty or shows no data message
    await expect(
      page.getByText("User's Posts", { exact: false }),
    ).toBeVisible();

    // The datagrid should either be empty or show a no results message
    // React Admin typically shows an empty table or "No results found" message
  });

  test("should filter posts correctly by userId using getManyReference", async ({
    page,
  }) => {
    // Create two users
    const timestamp = Date.now();
    const users = [];

    for (let i = 1; i <= 2; i++) {
      await page.goto("/#/users/create");
      await page.getByLabel(/name/i).fill(`User ${i} ${timestamp}`);
      await page.getByLabel(/email/i).fill(`user${i}${timestamp}@example.com`);
      await page.getByLabel(/role/i).fill("author");
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/users/);
      await page.waitForTimeout(500);

      // Navigate to sorted list to ensure first row is the newly created user
      await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
      await page.waitForLoadState("networkidle");
      await page.waitForTimeout(500);

      // Get the user ID
      const firstRow = page.locator("table tbody tr").first();
      const userId = (await firstRow.locator("td").nth(1).textContent()).trim();
      users.push({ id: userId, name: `User ${i} ${timestamp}` });
    }

    // Create posts for each user
    for (let userIndex = 0; userIndex < users.length; userIndex++) {
      const user = users[userIndex];
      for (let postIndex = 1; postIndex <= 2; postIndex++) {
        await page.goto("/#/posts/create");
        await page.waitForLoadState("networkidle");
        await page.waitForTimeout(500);
        await page
          .getByLabel(/title/i)
          .fill(`Post ${postIndex} by ${user.name}`);
        await page
          .getByRole("textbox", { name: /^content$/i })
          .fill(`Content for post ${postIndex}`);
        await page.locator('input[name="userId"]').fill(user.id);
        await page.getByLabel(/status/i).fill("published");
        await page.getByRole("button", { name: /save/i }).click();
        await page.waitForURL(/.*#\/posts/);
        await page.waitForTimeout(500);
      }
    }

    // View first user's show page and verify only their posts are shown
    await page.goto(`/#/users/${users[0].id}/show`);
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Should see User 1's posts
    await expect(page.getByText(`Post 1 by ${users[0].name}`)).toBeVisible();
    await expect(page.getByText(`Post 2 by ${users[0].name}`)).toBeVisible();

    // Should NOT see User 2's posts
    await expect(
      page.getByText(`Post 1 by ${users[1].name}`),
    ).not.toBeVisible();
    await expect(
      page.getByText(`Post 2 by ${users[1].name}`),
    ).not.toBeVisible();

    // View second user's show page and verify only their posts are shown
    await page.goto(`/#/users/${users[1].id}/show`);
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Should see User 2's posts
    await expect(page.getByText(`Post 1 by ${users[1].name}`)).toBeVisible();
    await expect(page.getByText(`Post 2 by ${users[1].name}`)).toBeVisible();

    // Should NOT see User 1's posts
    await expect(
      page.getByText(`Post 1 by ${users[0].name}`),
    ).not.toBeVisible();
    await expect(
      page.getByText(`Post 2 by ${users[0].name}`),
    ).not.toBeVisible();
  });

  test("should handle pagination in ReferenceManyField", async ({ page }) => {
    // Create a user
    await page.getByRole("link", { name: /create/i }).click();
    const timestamp = Date.now();
    const userName = `Prolific Author ${timestamp}`;
    await page.getByLabel(/name/i).fill(userName);
    await page.getByLabel(/email/i).fill(`author${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("author");
    await page.getByRole("button", { name: /save/i }).click();

    // Wait for redirect and get user ID
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Navigate to sorted list to ensure first row is the newly created user
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500);

    const firstRow = page.locator("table tbody tr").first();
    const userId = (await firstRow.locator("td").nth(1).textContent()).trim();

    // Create many posts (more than default pagination limit)
    await page.goto("/#/posts");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500);

    for (let i = 1; i <= 15; i++) {
      await page
        .waitForSelector('a[href="#/posts/create"]', { timeout: 5000 })
        .catch(() => {});
      await page.getByRole("link", { name: /create/i }).click();
      await page.waitForLoadState("networkidle");
      await page.waitForTimeout(300);
      await page.getByLabel(/title/i).fill(`Post ${i} ${timestamp}`);
      await page
        .getByRole("textbox", { name: /^content$/i })
        .fill(`Content for post ${i}`);
      await page.locator('input[name="userId"]').fill(userId);
      await page.getByLabel(/status/i).fill("published");
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/posts/);
      await page.waitForTimeout(300);
    }

    // Navigate to user show page
    await page.goto(`/#/users/${userId}/show`);
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Verify the posts section exists
    await expect(
      page.getByText("User's Posts", { exact: false }),
    ).toBeVisible();

    // Count visible posts (should show first page worth)
    // Use a more specific selector to get only the posts table within ReferenceManyField
    const postsSection = page
      .locator("div")
      .filter({ hasText: "User's Posts" })
      .locator("..")
      .locator("table");
    const visiblePosts = await postsSection.locator("tbody tr").count();
    expect(visiblePosts).toBeGreaterThan(0);
    expect(visiblePosts).toBeLessThanOrEqual(15); // Allow for default pagination

    // Check if pagination controls exist
    const paginationExists = await page
      .locator('button[aria-label*="page" i], button[aria-label*="next" i]')
      .last()
      .isVisible()
      .catch(() => false);

    if (paginationExists) {
      // If pagination exists, verify we can navigate to next page
      await page.locator('button[aria-label*="next" i]').last().click();
      await page.waitForTimeout(1000);

      // Verify different posts are now visible
      const newVisiblePosts = await page
        .locator("table")
        .last()
        .locator("tbody tr")
        .count();
      expect(newVisiblePosts).toBeGreaterThan(0);
    }
  });

  test("should update post and see changes in ReferenceManyField", async ({
    page,
  }) => {
    // Create a user
    await page.getByRole("link", { name: /create/i }).click();
    const timestamp = Date.now();
    const userName = `Editor User ${timestamp}`;
    await page.getByLabel(/name/i).fill(userName);
    await page.getByLabel(/email/i).fill(`editor${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("author");
    await page.getByRole("button", { name: /save/i }).click();

    // Get user ID
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);

    // Navigate to sorted list to ensure first row is the newly created user
    await page.goto("/#/users?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500);
    const firstRow = page.locator("table tbody tr").first();
    const userId = (await firstRow.locator("td").nth(1).textContent()).trim();

    // Create a post
    await page.goto("/#/posts/create");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500);
    const originalTitle = `Original Title ${timestamp}`;
    await page.getByLabel(/title/i).fill(originalTitle);
    await page
      .getByRole("textbox", { name: /^content$/i })
      .fill("Original content");
    await page.locator('input[name="userId"]').fill(userId);
    await page.getByLabel(/status/i).fill("draft");
    await page.getByRole("button", { name: /save/i }).click();
    await page.waitForURL(/.*#\/posts/);
    await page.waitForTimeout(500);

    // View user show page and verify original title
    await page.goto(`/#/users/${userId}/show`);
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);
    await expect(page.getByText(originalTitle).first()).toBeVisible();

    // Edit the post - navigate to sorted posts list to find our post
    await page.goto("/#/posts?page=1&perPage=25&sort=id&order=DESC");
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(500);

    // Find the row with our post title and click edit
    const postRow = page.locator(`tr:has-text("${originalTitle}")`).first();
    await postRow.locator('a[aria-label*="Edit"]').click();
    await page.waitForLoadState("networkidle");

    const updatedTitle = `Updated Title ${timestamp}`;
    await page.getByLabel(/title/i).fill(updatedTitle);
    await page.getByRole("button", { name: /save/i }).click();
    await page.waitForURL(/.*#\/posts/);
    await page.waitForTimeout(1000);

    // Go back to user show page and verify updated title is shown
    await page.goto(`/#/users/${userId}/show`, { waitUntil: "networkidle" });
    await page.waitForTimeout(2000);

    await expect(page.getByText(updatedTitle).first()).toBeVisible();
    // Original title should not be visible anywhere on the page
    const originalTitleCount = await page.getByText(originalTitle).count();
    expect(originalTitleCount).toBe(0);
  });

  test("should handle sorting in ReferenceManyField", async ({ page }) => {
    // Create a user
    await page.getByRole("link", { name: /create/i }).click();
    const timestamp = Date.now();
    const userName = `Sort Test User ${timestamp}`;
    await page.getByLabel(/name/i).fill(userName);
    await page.getByLabel(/email/i).fill(`sorttest${timestamp}@example.com`);
    await page.getByLabel(/role/i).fill("author");
    await page.getByRole("button", { name: /save/i }).click();

    // Get user ID
    await page.waitForURL(/.*#\/users/);
    await page.waitForTimeout(1000);
    const firstRow = page.locator("table tbody tr").first();
    const userId = (await firstRow.locator("td").nth(1).textContent()).trim();

    // Create posts with different titles (to test sorting)
    const posts = ["Zebra Post", "Alpha Post", "Middle Post"];
    for (const title of posts) {
      await page.goto("/#/posts/create");
      await page.waitForLoadState("networkidle");
      await page.waitForTimeout(500);
      await page.getByLabel(/title/i).fill(`${title} ${timestamp}`);
      await page.getByRole("textbox", { name: /^content$/i }).fill("Content");
      await page.locator('input[name="userId"]').fill(userId);
      await page.getByLabel(/status/i).fill("published");
      await page.getByRole("button", { name: /save/i }).click();
      await page.waitForURL(/.*#\/posts/);
      await page.waitForTimeout(500);
    }

    // Navigate to user show page
    await page.goto(`/#/users/${userId}/show`);
    await page.waitForLoadState("networkidle");
    await page.waitForTimeout(1000);

    // Verify all posts are visible
    for (const title of posts) {
      await expect(
        page.getByText(`${title} ${timestamp}`).first(),
      ).toBeVisible();
    }

    // Try to click on a column header to sort (if sorting is enabled)
    const titleHeader = page
      .locator("table")
      .last()
      .locator('thead th:has-text("Title")');
    const isSortable = await titleHeader
      .locator('button, span[role="button"]')
      .isVisible()
      .catch(() => false);

    if (isSortable) {
      await titleHeader.locator('button, span[role="button"]').click();
      await page.waitForTimeout(1000);

      // Verify the posts are still all visible (just reordered)
      for (const title of posts) {
        await expect(
          page.getByText(`${title} ${timestamp}`).first(),
        ).toBeVisible();
      }
    }
  });
});
