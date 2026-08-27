import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

class TrieNode {
    children: Map<string, TrieNode> = new Map();
    isEnd: boolean = false;
    word: string = '';
    level: number = 1;
}

export class DFAEngine {
    private root: TrieNode = new TrieNode();
    private isInitialized: boolean = false;

    async init() {
        this.root = new TrieNode();
        const words = await prisma.sensitiveWord.findMany();
        for (const w of words) {
            this.addWord(w.word, w.level);
        }
        this.isInitialized = true;
        console.log(`[DFA] Loaded ${words.length} sensitive words into Trie.`);
    }

    addWord(word: string, level: number = 1) {
        let node = this.root;
        for (const char of word) {
            if (!node.children.has(char)) {
                node.children.set(char, new TrieNode());
            }
            node = node.children.get(char)!;
        }
        node.isEnd = true;
        node.word = word;
        node.level = level;
    }

    match(text: string): { matched: boolean; words: {word: string, level: number}[] } {
        if (!this.isInitialized) return { matched: false, words: [] };

        const matchedWords = new Set<{word: string, level: number}>();
        for (let i = 0; i < text.length; i++) {
            let node = this.root;
            for (let j = i; j < text.length; j++) {
                const char = text[j];
                if (!node.children.has(char)) break;
                
                node = node.children.get(char)!;
                if (node.isEnd) {
                    matchedWords.add({word: node.word, level: node.level});
                }
            }
        }
        
        // Remove duplicates easily by spreading
        const result = Array.from(matchedWords).filter((v, i, a) => a.findIndex(t => (t.word === v.word)) === i);
        
        return {
            matched: result.length > 0,
            words: result
        };
    }
}

export const dfaEngine = new DFAEngine();
