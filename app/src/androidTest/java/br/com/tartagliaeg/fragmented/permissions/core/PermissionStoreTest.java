package br.com.tartagliaeg.fragmented.permissions.core;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by tartaglia on 11/26/17.
 * ...
 */
@RunWith(AndroidJUnit4.class)
public class PermissionStoreTest {
  private static final String SP_NAME = PermissionStore.SP_NAME;

  private SharedPreferences getSharedPreferences() {
    return InstrumentationRegistry.getTargetContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
  }

  @Before
  @After
  public void clearSharedPreferences() {
    getSharedPreferences().edit().clear().commit();
  }

  @Test
  public void savePermissionShouldPersistTheGivenPermissionsIntoSharedPreferences() throws Exception {
    String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    Context context = InstrumentationRegistry.getTargetContext();
    PermissionStore store = new PermissionStore();
    store.start(context);

    store.savePermission(new Permission(
      ACCESS_FINE_LOCATION,
      false,
      false,
      false
    ));

    store.savePermission(new Permission(
      ACCESS_COARSE_LOCATION,
      true,
      true,
      false
    ));

    assertFalse(getSharedPreferences().getBoolean(PermissionStore.SP_PERMISSION_ASKED + ACCESS_FINE_LOCATION, true));
    assertFalse(getSharedPreferences().getBoolean(PermissionStore.SP_PERMISSION_GRANTED + ACCESS_FINE_LOCATION, true));
    assertFalse(getSharedPreferences().getBoolean(PermissionStore.SP_PERMISSION_NOT_ASK_AGAIN + ACCESS_FINE_LOCATION, true));

    assertTrue(getSharedPreferences().getBoolean(PermissionStore.SP_PERMISSION_ASKED + ACCESS_COARSE_LOCATION, false));
    assertTrue(getSharedPreferences().getBoolean(PermissionStore.SP_PERMISSION_GRANTED + ACCESS_COARSE_LOCATION, false));
    assertFalse(getSharedPreferences().getBoolean(PermissionStore.SP_PERMISSION_NOT_ASK_AGAIN + ACCESS_COARSE_LOCATION, true));
  }

  @Test
  public void shouldThrowIllegalStateExceptionWhenCallingMethodsBeforeStartWasCalled() {
    PermissionStore store = new PermissionStore();
    int exceptionsThrown = 0;

    try {
      store.savePermission(new Permission("some", false, false, false));
    } catch (IllegalStateException e) {
      exceptionsThrown++;
    }

    try {
      store.stop();
    } catch (IllegalStateException e) {
      exceptionsThrown++;
    }

    try {
      store.isPermissionAsked("some-permission");
    } catch (IllegalStateException e) {
      exceptionsThrown++;
    }

    try {
      store.isPermissionGranted("some-permission");
    } catch (IllegalStateException e) {
      exceptionsThrown++;
    }

    try {
      store.isPermissionNotAskAgain("some-permission");
    } catch (IllegalStateException e) {
      exceptionsThrown++;
    }

    assertEquals(exceptionsThrown, 5);
  }


}