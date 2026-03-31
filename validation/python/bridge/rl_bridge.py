import os
import random
import pickle
from typing import Optional

import numpy as np

try:
    import torch
    TORCH_AVAILABLE = True
except Exception:  # pragma: no cover
    TORCH_AVAILABLE = False
    torch = None  # type: ignore


class PythonRLBridge(object):
    """Minimal RL bridge for validation.

    - If a Q-Learning pickle is provided, it is expected to contain a policy
      with a ``predict(state) -> action`` method or a mapping-like object. If it
      can't be used, we fallback to random actions.
    - If a DQN PyTorch model is provided, we run a forward pass and pick argmax.
    """

    def __init__(self, qlearning_model_path: str = "models/qlearning_cloudsim.pkl",
                 dqn_model_path: str = "models/dqn_cloudsim.pt") -> None:
        self._q_path = qlearning_model_path
        self._dqn_path = dqn_model_path
        self._q_model = self._load_q_model(qlearning_model_path)
        self._dqn_model = self._load_dqn_model(dqn_model_path)
        self._last_action = 0

    # === Utility loaders ===
    def _load_q_model(self, path: str):
        if not path or not os.path.exists(path):
            return None
        try:
            with open(path, "rb") as f:
                return pickle.load(f)
        except Exception:
            return None

    def _load_dqn_model(self, path: str):
        if not TORCH_AVAILABLE or not path or not os.path.exists(path):
            return None
        try:
            model = torch.load(path, map_location="cpu")
            model.eval()
            return model
        except Exception:
            return None

    # === Methods used by Java ===
    def isQLearningReady(self) -> bool:
        return self._q_model is not None

    def isDQNReady(self) -> bool:
        return self._dqn_model is not None

    def selectActionQLearning(self, state: list) -> int:
        s = np.array(state, dtype=np.float32)
        # Try predict(state) if available
        try:
            if hasattr(self._q_model, "predict"):
                a = self._q_model.predict(s)
                if isinstance(a, (list, tuple, np.ndarray)):
                    a = int(np.argmax(a))
                return int(a)
        except Exception:
            pass
        # Mapping-like lookup by tuple key
        try:
            if hasattr(self._q_model, "get"):
                key = tuple(np.round(s, 2))
                a = self._q_model.get(key, None)
                if a is not None:
                    return int(a)
        except Exception:
            pass
        # Fallback random action among {0,1,2}
        return int(random.randint(0, 2))

    def selectActionDQN(self, state: list) -> int:
        s = np.array(state, dtype=np.float32)
        if self._dqn_model is not None and TORCH_AVAILABLE:
            try:
                with torch.no_grad():
                    x = torch.tensor(s).unsqueeze(0)
                    q = self._dqn_model(x)
                    if hasattr(q, "detach"):
                        q = q.detach().cpu().numpy()
                    return int(np.argmax(q))
            except Exception:
                pass
        return int(random.randint(0, 2))

    # Online updates are optional for validation; no-ops are fine
    def updateQLearning(self, reward: float, nextState: list, done: bool) -> None:
        self._last_action = 0

    def updateDQN(self, reward: float, nextState: list, done: bool) -> None:
        self._last_action = 0

    # Py4J expects a getClass method name sometimes
    class Java:
        implements = ["org.tcdrm.adaptive.rl.PythonRLBridge"]
