package pset.three;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Eric Crosson {@literal <eric.s.crosson@utexas.edu>}
 * @author William "Stormy" Mauldin {@literal <stormymauldin@utexas.edu>}
 * @version 0.1
 * @since 2016-02-29
 */

public class ServerTest {
	/**
	 * Test normal functionality of purchase using UDP protocol
	 */
	@Test
	public void normalUdpPurchase() {

	}

	/**
	 * Test normal functionality of purchase using TCP protocol
	 */
	@Test
	public void normalTcpPurchase() {

	}

	/**
	 * Test functionality of purchase using UDP protocol when user attempts to
	 * purchase item not in database
	 */
	@Test
	public void noSuchItemUdpPurchase() {

	}

	/**
	 * Test functionality of purchase using TCP protocol when user attempts to
	 * purchase item not in database
	 */
	@Test
	public void noSuchItemTcpPurchase() {

	}

	/**
	 * Test functionality of purchase using UDP protocol when user attempts to
	 * purchase item of quantity less than available
	 */
	@Test
	public void insufficientQuantityUdpPurchase() {

	}

	/**
	 * Test functionality of purchase using TCP protocol when user attempts to
	 * purchase item of quantity less than available
	 */
	@Test
	public void insufficientQuantityTcpPurchase() {

	}

	/**
	 * Test normal functionality of cancel using UDP protocol
	 */
	@Test
	public void normalUdpCancel() {

	}

	/**
	 * Test normal functionality of cancel using TCP protocol
	 */
	@Test
	public void normalTcpCancel() {

	}

	/**
	 * Test functionality of cancel using UDP protocol wherein the user did not
	 * make the purchase in the first place
	 */
	@Test
	public void noInitialPurchaseUdpCancel() {

	}

	/**
	 * Test functionality of cancel using TCP protocol wherein the user did not
	 * make the purchase in the first place
	 */
	@Test
	public void noInitialPurchaseTcpCancel() {

	}

	/**
	 * Test normal functionality of search using UDP protocol. User has past
	 * orders, none of which are cancelled.
	 */
	@Test
	public void normalUdpSearch() {

	}

	/**
	 * Test normal functionality of search using TCP protocol. User has past
	 * orders, none of which are cancelled.
	 */
	@Test
	public void normalTcpSearch() {

	}

	/**
	 * Test functionality of search using UDP protocol wherein user has past
	 * orders, some, but not all, of which are cancelled. Even cancelled orders
	 * should be reported.
	 */
	@Test
	public void someCancelledOrdersUdpSearch() {

	}

	/**
	 * Test functionality of search using TCP protocol wherein user has past
	 * orders, some, but not all, of which are cancelled. Both types of orders
	 * should be reported, with the cancelled ones indicated.
	 */
	@Test
	public void someCancelledOrdersTcpSearch() {

	}

	/**
	 * Test functionality of search using UDP protocol wherein user has past
	 * orders, all of which are cancelled.
	 */
	@Test
	public void allCancelledOrdersUdpSearch() {

	}

	/**
	 * Test functionality of search using TCP protocol wherein user has past
	 * orders, all of which are cancelled.
	 */
	@Test
	public void allCancelledOrdersTcpSearch() {

	}

	/**
	 * Test functionality of search using UDP protocol wherein user has no past
	 * orders.
	 */
	@Test
	public void noOrdersUdpSearch() {

	}

	/**
	 * Test functionality of search using TCP protocol wherein user has no past
	 * orders.
	 */
	@Test
	public void noOrdersTcpSearch() {

	}
}
