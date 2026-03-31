import argparse
import time
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters
from .rl_bridge import PythonRLBridge


def connect_and_register(port: int, q_path: str, dqn_path: str):
    print(f"📡 Connecting to Java Gateway (port {port})...")
    gw = JavaGateway(
        gateway_parameters=GatewayParameters(port=port),
        callback_server_parameters=CallbackServerParameters()
    )
    bridge = PythonRLBridge(qlearning_model_path=q_path, dqn_model_path=dqn_path)
    gw.entry_point.registerPythonBridge(bridge)

    print("✅ Connected!")
    print(f"  - Q-Learning ready: {bridge.isQLearningReady()}")
    print(f"  - DQN ready: {bridge.isDQNReady()}")
    return gw


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--qlearning-model', default='models/qlearning_cloudsim.pkl')
    parser.add_argument('--dqn-model', default='models/dqn_cloudsim.pt')
    parser.add_argument('--port', type=int, default=25333)
    args = parser.parse_args()

    gw = connect_and_register(args.port, args.qlearning_model, args.dqn_model)

    print("\n🎯 Ready. Press Ctrl+C to stop.")
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        gw.shutdown()
        print("🛑 Stopped.")


if __name__ == '__main__':
    main()
