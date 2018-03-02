package br.com.tartagliaeg.fragmented.permissions.core;

import android.content.pm.PackageManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by tartaglia on 11/25/17.
 * ...
 */
@RunWith(MockitoJUnitRunner.class)
public class PermissionManagerTest {
  @Test
  public void shouldThrowIllegalStateExceptionWhenCallingMethodsBeforeStartWasCalled() {
    PermissionManager manager = new PermissionManager();
    int exceptionsThrown = 0;

    try {
      manager.retrievePermission("some-permission");
    } catch (IllegalStateException e) {
      exceptionsThrown++;
    }

    try {
      manager.askForNotAskedPermissions("some-permission");
    } catch (IllegalStateException e) {
      exceptionsThrown++;
    }

    try {
      manager.askForPermissions("some-permission");
    } catch (IllegalStateException e) {
      exceptionsThrown++;
    }

    try {
      manager.handlePermissionResult(new String[]{"some-permission"}, new int[]{1});
    } catch (IllegalStateException e) {
      exceptionsThrown++;
    }

    try {
      manager.destroy();
    } catch (IllegalStateException e) {
      exceptionsThrown++;
    }

    assertEquals(exceptionsThrown, 5);
  }

  @Test
  public void shouldUseTheStoreToRetrievePermissions() throws Exception {
    IPermission.Fragment fragment = Mockito.mock(IPermission.Fragment.class);
    IPermission.Store store = Mockito.mock(IPermission.Store.class);

    PermissionManager manager = new PermissionManager();
    PermissionManager.StashedProperties props = new PermissionManager.StashedProperties();

    manager.create(fragment, store, props);

    when(store.isPermissionAsked("some_permission")).thenReturn(true);
    when(store.isPermissionGranted("some_permission")).thenReturn(true);
    when(store.isPermissionNotAskAgain("some_permission")).thenReturn(false);

    Permission perm = manager.retrievePermission("some_permission");

    assertTrue(perm.isAsked());
    assertTrue(perm.isGranted());
    assertFalse(perm.isNotAskAgain());
    assertEquals(perm.getPermissionName(), "some_permission");

    Mockito.verifyZeroInteractions(fragment);
  }


  @Test
  public void shouldSkipThePermissionPromptPhaseWhenItIsWaitingForPermissionDialogResponse() {
    IPermission.Fragment fragment = Mockito.mock(IPermission.Fragment.class);
    IPermission.Store store = Mockito.mock(IPermission.Store.class);

    PermissionManager manager = new PermissionManager();
    PermissionManager.StashedProperties props = new PermissionManager.StashedProperties();
    props.mIsWaitingPermissionDialogResponse = true;

    manager.create(fragment, store, props);
    Observable<IPermission.Retriever> obs1 = manager.askForPermissions("some-permission");
    Observable<IPermission.Retriever> obs2 = manager.askForNotAskedPermissions("some-permission");

    assertNotNull(obs1);
    assertNotNull(obs2);

    Mockito.verifyZeroInteractions(fragment);
    Mockito.verifyZeroInteractions(store);
  }

  @Test
  public void shouldSkipThePermissionPromptPhaseWhenNoPermissionParamsWereGiven() {
    IPermission.Fragment fragment = Mockito.mock(IPermission.Fragment.class);
    IPermission.Store store = Mockito.mock(IPermission.Store.class);

    PermissionManager manager = new PermissionManager();
    PermissionManager.StashedProperties props = new PermissionManager.StashedProperties();

    manager.create(fragment, store, props);
    Observable<IPermission.Retriever> obs1 = manager.askForPermissions();
    Observable<IPermission.Retriever> obs2 = manager.askForNotAskedPermissions();

    assertNotNull(obs1);
    assertNotNull(obs2);

    Mockito.verifyZeroInteractions(fragment);
    Mockito.verifyZeroInteractions(store);
  }

  @Test
  public void shouldSkipThePermissionPromptPhaseWhenAllThePermissionsWereGrantedAlready() {
    IPermission.Fragment fragment = Mockito.mock(IPermission.Fragment.class);
    IPermission.Store store = Mockito.mock(IPermission.Store.class);

    PermissionManager manager = new PermissionManager();
    PermissionManager.StashedProperties props = new PermissionManager.StashedProperties();

    manager.create(fragment, store, props);

    when(store.isPermissionGranted("permission1")).thenReturn(true);
    when(store.isPermissionGranted("permission2")).thenReturn(true);

    Observable<IPermission.Retriever> obs1 = manager.askForPermissions("permission1", "permission2");
    assertNotNull(obs1);
    Mockito.verify(store, Mockito.times(1)).isPermissionGranted("permission1");
    Mockito.verify(store, Mockito.times(1)).isPermissionGranted("permission2");

    Mockito.reset(store);

    when(store.isPermissionGranted("permission1")).thenReturn(true);
    when(store.isPermissionGranted("permission2")).thenReturn(true);
    when(store.isPermissionAsked("permission1")).thenReturn(false);
    when(store.isPermissionAsked("permission2")).thenReturn(false);

    Observable<IPermission.Retriever> obs2 = manager.askForNotAskedPermissions("permission1", "permission2");
    assertNotNull(obs2);
    Mockito.verify(store, Mockito.times(1)).isPermissionGranted("permission1");
    Mockito.verify(store, Mockito.times(1)).isPermissionGranted("permission2");

    Mockito.verifyZeroInteractions(fragment);
  }


  @Test
  public void askForPermissionsShouldRunThroughThePermissionPromptPhase() {
    IPermission.Fragment fragment = Mockito.mock(IPermission.Fragment.class);
    IPermission.Store store = Mockito.mock(IPermission.Store.class);

    PermissionManager manager = new PermissionManager();
    PermissionManager.StashedProperties props = new PermissionManager.StashedProperties();
    props.mIsWaitingPermissionDialogResponse = false;

    when(store.isPermissionGranted("permission1")).thenReturn(true);
    when(store.isPermissionGranted("permission2")).thenReturn(false);

    manager.create(fragment, store, props);
    Observable<IPermission.Retriever> obs1 = manager.askForPermissions("permission1", "permission2");

    assertNotNull(obs1);

    Mockito.verify(fragment, Mockito.times(1)).showPermissionDialog(new String[]{"permission1", "permission2"});
    assertTrue(props.mIsWaitingPermissionDialogResponse);
  }

  @Test
  public void askForNotAskedPermissionsShouldRunThroughThePermissionPromptPhase() {
    IPermission.Fragment fragment = Mockito.mock(IPermission.Fragment.class);
    IPermission.Store store = Mockito.mock(IPermission.Store.class);

    PermissionManager manager = new PermissionManager();
    PermissionManager.StashedProperties props = new PermissionManager.StashedProperties();
    props.mIsWaitingPermissionDialogResponse = false;

    when(store.isPermissionAsked("permission1")).thenReturn(true);
    when(store.isPermissionAsked("permission2")).thenReturn(false);
    when(store.isPermissionAsked("permission3")).thenReturn(true);
    when(store.isPermissionAsked("permission4")).thenReturn(false);
    when(store.isPermissionGranted(Mockito.anyString())).thenReturn(false);

    manager.create(fragment, store, props);
    Observable<IPermission.Retriever> obs1 = manager
      .askForNotAskedPermissions("permission1", "permission2", "permission3", "permission4");

    assertNotNull(obs1);

    Mockito.verify(fragment, Mockito.times(1)).showPermissionDialog(new String[]{"permission2", "permission4"});
    assertTrue(props.mIsWaitingPermissionDialogResponse);
  }

  @Test(expected = IllegalStateException.class)
  public void handlePermissionsResultShouldThrowErrorWhenCalledBeforeAskForPermissions() {
    IPermission.Fragment fragment = Mockito.mock(IPermission.Fragment.class);
    IPermission.Store store = Mockito.mock(IPermission.Store.class);

    PermissionManager manager = new PermissionManager();
    PermissionManager.StashedProperties props = new PermissionManager.StashedProperties();
    props.mIsWaitingPermissionDialogResponse = false;

    manager.create(fragment, store, props);
    manager.handlePermissionResult(new String[]{"permission1"}, new int[]{PackageManager.PERMISSION_GRANTED});

  }

  @Test
  public void handlePermissionsResultShouldCheckNAAAndPersistPermission() {
    IPermission.Fragment fragment = Mockito.mock(IPermission.Fragment.class);
    IPermission.Store store = Mockito.mock(IPermission.Store.class);

    PermissionManager manager = new PermissionManager();
    PermissionManager.StashedProperties props = new PermissionManager.StashedProperties();
    props.mIsWaitingPermissionDialogResponse = true;

    when(fragment.canAskPermissionAgain("permission2")).thenReturn(true);
    when(fragment.canAskPermissionAgain("permission4")).thenReturn(false);

    manager.create(fragment, store, props);
    manager
      .handlePermissionResult(new String[]{"permission1", "permission2", "permission3", "permission4"}, new int[]{
        PackageManager.PERMISSION_GRANTED,
        PackageManager.PERMISSION_DENIED,
        PackageManager.PERMISSION_GRANTED,
        PackageManager.PERMISSION_DENIED,
      });

    assertFalse(props.mIsWaitingPermissionDialogResponse);

    Mockito.verify(store, Mockito.times(1))
      .savePermission(Mockito.eq(new Permission("permission1", true, true, false)));
    Mockito.verify(store, Mockito.times(1))
      .savePermission(Mockito.eq(new Permission("permission2", false, true, false)));
    Mockito.verify(store, Mockito.times(1))
      .savePermission(Mockito.eq(new Permission("permission3", true, true, false)));
    Mockito.verify(store, Mockito.times(1))
      .savePermission(Mockito.eq(new Permission("permission4", false, true, true)));

    Mockito.verify(fragment, Mockito.times(0)).canAskPermissionAgain("permission1");
    Mockito.verify(fragment, Mockito.times(0)).canAskPermissionAgain("permission3");
  }

}