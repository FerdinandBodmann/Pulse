const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- Unauthenticated tests ---
test("Unauthenticated user: cannot read posts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("posts").get());
});

test("Unauthenticated user: cannot create post", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("posts").add({
      userId: ALICE_UID,
      authorName: "Alice",
      authorHandle: "alice",
      content: "Hello world",
      category: "General",
      createdAt: new Date(),
    })
  );
});

// --- Authenticated user tests ---
test("Authenticated user: can create valid post", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("posts").doc("post_alice_1").set({
      userId: ALICE_UID,
      authorName: "Alice",
      authorHandle: "alice",
      content: "Excited to join Pulse! #PulseCommunity",
      category: "General",
      likesCount: 0,
      commentsCount: 0,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create post spoofing other user's userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("posts").doc("post_spoof_1").set({
      userId: BOB_UID, // Spoofed!
      authorName: "Bob",
      authorHandle: "bob",
      content: "Spoofed message",
      category: "General",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can create and read their profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice_w",
      displayName: "Alice Wonderland",
      bio: "Building amazing Android apps",
      followersCount: 0,
      followingCount: 0,
      postsCount: 0,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: cannot update another user's profile", async () => {
  // Seed Alice profile with security rules disabled
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice_w",
      displayName: "Alice Wonderland",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hacked by Bob",
    })
  );
});

test("Authenticated user: can like a post with own userId", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("posts").doc("post_1").set({
      userId: BOB_UID,
      authorName: "Bob",
      authorHandle: "bob",
      content: "Bob's great post",
      category: "Tech",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").collection("likes").doc(ALICE_UID).set({
      userId: ALICE_UID,
      postId: "post_1",
      createdAt: new Date(),
    })
  );
});
