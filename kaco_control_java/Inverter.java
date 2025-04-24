import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.ed.edcom.Client;
import com.ed.edcom.ClientListener;
import com.ed.edcom.Discovery;
import com.ed.edcom.Util;

import com.ed.data.Settings;

import io.openems.common.utils.InetAddressUtils;


public class Inverter {

    private Client client = null;

    // katek inverter settings is instantiated in
    // io.openems.edge.kaco.blueplanet.hybrid10/src/io/openems/edge/kaco/blueplanet/hybrid10/core/BpData.java
    private Settings settings;

    public static void main(String[] args) {
        // Check if the correct number of arguments is provided
        if (args.length < 4) {
            System.out.println("Usage: java Inverter <IP Address> <User Password> <IdentKeyHex> <p_ac_setpoint_W>");
            System.exit(1);
        }

        // Parse input arguments
        String ip_addr = args[0];
        String user_pw = args[1];
        String kacoBlueplanetHybrid10IdentKey = args[2];
        float p_ac_setpoint_W;

        try {
            p_ac_setpoint_W = Float.parseFloat(args[3]);  // Convert third argument to float
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid float value for p_ac_setpoint_W: '" + args[2] + "'");
            System.exit(1);
            return;  // Ensure exit to avoid further execution
        }

        // Print received arguments
        System.out.println("IP Address:      " + ip_addr);
        System.out.println("User Password:   " + user_pw);
        System.out.println("IdentKey:        " + kacoBlueplanetHybrid10IdentKey);
        System.out.println("p_ac_setpoint_W: " + p_ac_setpoint_W);


        Inverter inverter = new Inverter();

        Util util = Util.getInstance();

        /*
         * Init and listener must be set at the beginning for edcom library > 8. There
         * is no possibility to separate between the kaco versions before
         * this.client.isConnected() is called.
         */
        util.init();

        util.setListener(new ClientListener() {

	        @Override
	        public byte[] updateIdentKey(byte[] randomKey) {
		        var identKeyString = kacoBlueplanetHybrid10IdentKey;
		        if (identKeyString.startsWith("0x")) {
			        identKeyString = identKeyString.substring(2);
		        }
		        byte[] identKey = new byte[identKeyString.length() / 2];
		        for (int i = 0; i < identKey.length; i++) {
			        int index = i * 2;
			        int j = Integer.parseInt(identKeyString.substring(index, index + 2), 16);
			        identKey[i] = (byte) j;
		        }

		        final var len = 8;
		        byte[] tmp = new byte[len];
		        System.arraycopy(identKey, 0, tmp, 0, len);
		        for (int i = 0; i < tmp.length && i < randomKey.length; i++) {
			        tmp[i] += randomKey[i];
		        }
		        for (int i = 0; i < 99; i++) {
			        tmp[i % len] += 1;
			        tmp[i % len] += tmp[(i + 10) % len];
			        tmp[(i + 3) % len] *= tmp[(i + 11) % len];
			        tmp[i % len] += tmp[(i + 7) % len];
		        }
		        return tmp;
	        }
        });


        try {
            inverter.settings = new Settings();
        } catch (Exception e) {
            System.err.println("Failed to initialize settings: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        final InetAddress inverterAddress = InetAddressUtils.parseOrNull(ip_addr);

	    if (inverterAddress != null) {
	        try {
		        inverter.initClient(inverterAddress, user_pw);
		    } catch (Exception e) {
		        System.out.println("Client init failed: " + e.getMessage());
		        return;
		    }
		}

        System.out.println("Client initialized and password set.");

        inverter.settings.setPacSetPoint(p_ac_setpoint_W);
        System.out.println("The included openEMS libraries should transmit the setpoint to the inverter now. After this program is stopped, the inverter will reset after 60 seconds depending on the inverter settings. If no external timeout is set in the inverter, the setpoint will be kept.");
        return;  // todo: listener is still running and program does not exit
    }

    // adapted code from
    // io.openems.edge.kaco.blueplanet.hybrid10/src/io/openems/edge/kaco/blueplanet/hybrid10/core/KacoBlueplanetHybrid10CoreImpl.java
	private void initClient(InetAddress inverterAddress, String user_pw) throws Exception {
		// Initialize the Client
        InetAddress localAddress = getMatchingLocalInetAddress(inverterAddress);
		//InetAddress localAddress = KacoBlueplanetHybrid10CoreImpl.getMatchingLocalInetAddress(inverterAddress);

        this.client = new Client(inverterAddress, localAddress, 1);

        // Set user password
        this.client.setUserPass(user_pw);

		// Initialize all DataSets
		//this._bpData = BpData.from(this.client);

		// Prepare array of all DataSets for convenience
		//this.all = new DataSet[] { battery, inverter, status, settings, energy, vectis, systemInfo };

		// Register DataSets with Client
		//Stream.of(this.all) //
			//	.forEach(d -> d.registerData(client));

		//this.settings = new Settings();
		// Register DataSets with Client
		this.settings.registerData(this.client);

		this.client.start();
	}


    // io.openems.edge.kaco.blueplanet.hybrid10/src/io/openems/edge/kaco/blueplanet/hybrid10/core/KacoBlueplanetHybrid10CoreImpl.java
    /**
     * Gets a local IP address which is able to access the given remote IP.
     *
     * @param inetAddress the remote IP
     * @return a local IP address or null if no match was found
     */
    public static InetAddress getMatchingLocalInetAddress(InetAddress inetAddress) {
	    try (DatagramSocket socket = new DatagramSocket()) {
		    socket.connect(inetAddress, 9760);
		    InetAddress localAddress = socket.getLocalAddress();

		    if (localAddress.isAnyLocalAddress()) {
			    return null;
		    } else {
			    return localAddress;
		    }
	    } catch (SocketException e) {
		    return null;
	    }
    }
}

